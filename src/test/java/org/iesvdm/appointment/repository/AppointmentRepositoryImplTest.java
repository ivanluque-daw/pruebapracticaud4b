package org.iesvdm.appointment.repository;

import org.assertj.core.api.Assertions;
import org.iesvdm.appointment.entity.Appointment;
import org.iesvdm.appointment.entity.AppointmentStatus;
import org.iesvdm.appointment.entity.Customer;
import org.iesvdm.appointment.entity.User;
import org.iesvdm.appointment.repository.impl.AppointmentRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Spy;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class AppointmentRepositoryImplTest {

    private Set<Appointment> appointments;

    private AppointmentRepository appointmentRepository;

    @BeforeEach
    public void setup() {
        appointments = new HashSet<>();
        appointmentRepository = new AppointmentRepositoryImpl(appointments);
    }

    /**
     * Crea 2 citas (Appointment) una con id 1 y otra con id 2,
     * resto de valores inventados.
     * Agrégalas a las citas (appointments) con la que
     * construyes el objeto appointmentRepository bajo test.
     * Comprueba que cuando invocas appointmentRepository.getOne con uno
     * de los id's anteriores recuperas obtienes el objeto.
     * Pero si lo invocas con otro id diferente recuperas null
     */
    @Test
    void getOneTest() {
        Appointment a1 = new Appointment();
        a1.setId(1);

        Appointment a2 = new Appointment();
        a2.setId(2);

        appointments.add(a1);
        appointments.add(a2);

        Assertions.assertThat(appointmentRepository.getOne(1)).isEqualTo(a1);
        Assertions.assertThat(appointmentRepository.getOne(3)).isNull();
    }

    /**
     * Crea 2 citas (Appointment) y guárdalas mediante
     * appointmentRepository.save.
     * Comprueba que la colección appointments
     * contiene sólo esas 2 citas.
     */
    @Test
    void saveTest() {
        Appointment a1 = new Appointment();
        a1.setId(1);

        Appointment a2 = new Appointment();
        a2.setId(2);

        appointmentRepository.save(a1);
        appointmentRepository.save(a2);

        Assertions.assertThat(appointments).containsExactly(a1, a2);
    }

    /**
     * Crea 2 citas (Appointment) una cancelada por un usuario y otra no,
     * (atención al estado de la cita, lee el código) y agrégalas mediante
     * appointmentRepository.save a la colección de appointments
     * Comprueba que mediante appointmentRepository.findCanceledByUser
     * obtienes la cita cancelada.
     */
    @Test
    void findCanceledByUserTest() {
        User user = new User(1, "Ivan", "Luque");
        Appointment a1 = new Appointment();
        a1.setId(1);
        a1.setCanceler(user);
        a1.setCanceledAt(LocalDateTime.of(2024, 5, 2, 12, 30));
        a1.setStatus(AppointmentStatus.CANCELED);

        Appointment a2 = new Appointment();
        a2.setId(2);
        a2.setStatus(AppointmentStatus.CONFIRMED);

        appointmentRepository.save(a1);
        appointmentRepository.save(a2);

        Assertions.assertThat(appointmentRepository.findCanceledByUser(1)).containsOnly(a1);
    }

    /**
     * Crea 3 citas (Appointment), 2 para un mismo cliente (Customer)
     * con sólo una cita de ellas presentando fecha de inicio (start)
     * y fin (end) dentro del periodo de búsqueda (startPeriod,endPeriod).
     * Guárdalas mediante appointmentRepository.save.
     * Comprueba que appointmentRepository.findByCustomerIdWithStartInPeroid
     * encuentra la cita en cuestión.
     * Nota: utiliza LocalDateTime.of(...) para crear los LocalDateTime
     */
    @Test
    void findByCustomerIdWithStartInPeroidTest() {
        Customer customer = new Customer();
        customer.setId(1);
        customer.setUserName("ivan");

        Appointment a1 = new Appointment();
        a1.setId(1);
        a1.setCustomer(customer);
        a1.setStart(LocalDateTime.of(2024, 3, 2, 12, 30));
        a1.setEnd(LocalDateTime.of(2024, 3, 5, 12, 30));

        Appointment a2 = new Appointment();
        a2.setId(2);
        a2.setCustomer(customer);
        a2.setStart(LocalDateTime.of(2024, 5, 2, 12, 30));
        a2.setEnd(LocalDateTime.of(2024, 5, 9, 12, 30));

        Appointment a3 = new Appointment();
        a3.setId(3);
        a3.setStart(LocalDateTime.of(2024, 3, 6, 12, 30));
        a3.setEnd(LocalDateTime.of(2024, 3, 7, 12, 30));

        appointmentRepository.save(a1);
        appointmentRepository.save(a2);
        appointmentRepository.save(a3);

        Assertions.assertThat(appointmentRepository.findByCustomerIdWithStartInPeroid(1, LocalDateTime.of(2024, 1, 2, 12, 30), LocalDateTime.of(2024, 4, 2, 12, 30))).containsOnly(a1);
    }


    /**
     * Crea 2 citas (Appointment) una planificada (SCHEDULED) con tiempo fin
     * anterior a la tiempo buscado por appointmentRepository.findScheduledWithEndBeforeDate
     * guardándolas mediante appointmentRepository.save para la prueba de findScheduledWithEndBeforeDate
     */
    @Test
    void findScheduledWithEndBeforeDateTest() {
        Appointment a1 = new Appointment();
        a1.setId(1);
        a1.setStart(LocalDateTime.of(2024, 3, 2, 12, 30));
        a1.setEnd(LocalDateTime.of(2024, 3, 5, 12, 30));
        a1.setStatus(AppointmentStatus.SCHEDULED);

        Appointment a2 = new Appointment();
        a2.setId(2);
        a2.setStart(LocalDateTime.of(2024, 5, 2, 12, 30));
        a2.setEnd(LocalDateTime.of(2024, 5, 9, 12, 30));
        a2.setStatus(AppointmentStatus.FINISHED);

        appointmentRepository.save(a1);
        appointmentRepository.save(a2);

        Assertions.assertThat(appointmentRepository.findScheduledWithEndBeforeDate(LocalDateTime.of(2024, 5, 2, 12, 30))).containsOnly(a1);
    }


    /**
     * Crea 3 citas (Appointment) planificadas (SCHEDULED)
     * , 2 para un mismo cliente, con una elegible para cambio (con fecha de inicio, start, adecuada)
     * y otra no.
     * La tercera ha de ser de otro cliente.
     * Guárdalas mediante appointmentRepository.save
     * Comprueba que getEligibleAppointmentsForExchange encuentra la correcta.
     */
    @Test
    void getEligibleAppointmentsForExchangeTest() {
        Customer c1 = new Customer();
        c1.setId(1);
        c1.setUserName("ivan");

        Customer c2 = new Customer();
        c2.setId(2);
        c2.setUserName("luque");

        Appointment a1 = new Appointment();
        a1.setId(1);
        a1.setCustomer(c1);
        a1.setStart(LocalDateTime.of(2024, 3, 2, 12, 30));
        a1.setEnd(LocalDateTime.of(2024, 3, 5, 12, 30));
        a1.setStatus(AppointmentStatus.SCHEDULED);

        Appointment a2 = new Appointment();
        a2.setId(2);
        a2.setCustomer(c1);
        a2.setStart(LocalDateTime.of(2024, 5, 18, 12, 30));
        a2.setEnd(LocalDateTime.of(2024, 5, 21, 12, 30));
        a2.setStatus(AppointmentStatus.SCHEDULED);

        Appointment a3 = new Appointment();
        a3.setId(3);
        a3.setCustomer(c2);
        a3.setStart(LocalDateTime.of(2024, 3, 6, 12, 30));
        a3.setEnd(LocalDateTime.of(2024, 3, 7, 12, 30));
        a3.setStatus(AppointmentStatus.SCHEDULED);

        appointmentRepository.save(a1);
        appointmentRepository.save(a2);
        appointmentRepository.save(a3);

        // Espero buscar por el id 1, pero busca por todo aquel que no sea del mismo id, por eso falla
        // Despues de haber hecho el test del exchange me doy cuenta que el método estaba hecho para esa impl por qué se quiere buscar por un id distinto, pero para este caso no funciona
        Assertions.assertThat(appointmentRepository.getEligibleAppointmentsForExchange(LocalDateTime.of(2024, 5, 1, 12, 30), 1)).containsOnly(a2);
    }


    /**
     * Igual que antes, pero ahora las 3 citas tienen que tener
     * clientes diferentes y 2 de ellas con fecha de inicio (start)
     * antes de la especificada en el método de búsqueda para
     * findExchangeRequestedWithStartBefore
     */
    @Test
    void findExchangeRequestedWithStartBeforeTest() {
        Customer c1 = new Customer();
        c1.setId(1);
        c1.setUserName("ivan");

        Customer c2 = new Customer();
        c2.setId(2);
        c2.setUserName("luque");

        Customer c3 = new Customer();
        c3.setId(3);
        c3.setUserName("manuel");

        Appointment a1 = new Appointment();
        a1.setId(1);
        a1.setCustomer(c1);
        a1.setStart(LocalDateTime.of(2024, 3, 2, 12, 30));
        a1.setEnd(LocalDateTime.of(2024, 3, 5, 12, 30));
        a1.setStatus(AppointmentStatus.EXCHANGE_REQUESTED);

        Appointment a2 = new Appointment();
        a2.setId(2);
        a2.setCustomer(c1);
        a2.setStart(LocalDateTime.of(2024, 5, 18, 12, 30));
        a2.setEnd(LocalDateTime.of(2024, 5, 21, 12, 30));
        a2.setStatus(AppointmentStatus.EXCHANGE_REQUESTED);

        Appointment a3 = new Appointment();
        a3.setId(3);
        a3.setCustomer(c3);
        a3.setStart(LocalDateTime.of(2024, 3, 6, 12, 30));
        a3.setEnd(LocalDateTime.of(2024, 3, 7, 12, 30));
        a3.setStatus(AppointmentStatus.EXCHANGE_REQUESTED);

        appointmentRepository.save(a1);
        appointmentRepository.save(a2);
        appointmentRepository.save(a3);

        Assertions.assertThat(appointmentRepository.findExchangeRequestedWithStartBefore(LocalDateTime.of(2024, 5, 1, 12, 30))).containsExactly(a1, a3);
    }
}
