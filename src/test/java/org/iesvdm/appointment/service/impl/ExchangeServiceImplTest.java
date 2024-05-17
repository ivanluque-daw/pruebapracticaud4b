package org.iesvdm.appointment.service.impl;

import net.bytebuddy.asm.Advice;
import org.assertj.core.api.Assertions;
import org.iesvdm.appointment.entity.*;
import org.iesvdm.appointment.repository.AppointmentRepository;
import org.iesvdm.appointment.repository.ExchangeRequestRepository;
import org.iesvdm.appointment.repository.impl.AppointmentRepositoryImpl;
import org.iesvdm.appointment.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public class ExchangeServiceImplTest {

    @Spy
    private AppointmentRepository appointmentRepository = new AppointmentRepositoryImpl(new HashSet<>());

    @Mock
    private NotificationService notificationService;

    @Mock
    private ExchangeRequestRepository exchangeRequestRepository;

    @InjectMocks
    private ExchangeServiceImpl exchangeService;

    private Customer customer1 = new Customer(1
            , "paco"
            , "1234"
            , new ArrayList<>());
    private Customer customer2 = new Customer(2
            , "pepe"
            , "1111"
            , new ArrayList<>());

    @Captor
    private ArgumentCaptor<Integer> appointmentIdCaptor;

    @Spy
    private Appointment appointment1 = new Appointment(LocalDateTime.of(2024, 6, 10, 6, 0)
            , LocalDateTime.of(2024, 6, 16, 18, 0)
            , null
            , null
            , AppointmentStatus.SCHEDULED
            , customer1
            , null
    );

    @Spy
    private Appointment appointment2 = new Appointment(LocalDateTime.of(2024, 5, 18, 8, 15)
            , LocalDateTime.of(2024, 5, 18, 10, 15)
            , null
            , null
            , AppointmentStatus.SCHEDULED
            , customer2
            , null
    );

    @BeforeEach
    public void setup() {

        MockitoAnnotations.initMocks(this);

    }

    /**
     * Crea un stub para appointmentRepository.getOne
     * que devuelva una cita (Appointment) que
     * cumple que la fecha/tiempo de inicio (start) es
     * al menos un día después de la fecha/tiempo de búsqueda (actual)
     * , junto con los parámetros de estar planificada (SCHEDULED) y
     * pertenecer al cliente con userId 3.
     * De este modo que al invocar exchangeServiceImpl.checkIfEligibleForExchange
     * se debe obtener true.
     */
    @Test
    void checkIfEligibleForExchange() {
        Customer c1 = new Customer(3
                , "ivan"
                , "luque"
                , new ArrayList<>());

        Mockito.when(appointment1.getCustomer()).thenReturn(c1);
        Mockito.when(appointmentRepository.getOne(1)).thenReturn(appointment1);

        Assertions.assertThat(exchangeService.checkIfEligibleForExchange(3, 1)).isTrue();
    }


    /**
     * Añade mediante appointementRepository.save
     * 2 citas (Appointment) de modo que la eligible
     * la 2a empieza más de 24 horas más tarde
     * y pertenece a un cliente (Customer) con id diferente del
     * cliente de la primera que será appointmentToExchange.
     * Se debe verificar la invocación de los métodos appointmentRepository.getOne
     * con el appointmentId pasado a capturar mediante el captor de id
     */
    @Test
    void getEligibleAppointmentsForExchangeTest() {
        appointment1.setId(1);
        appointment2.setId(2);

        appointmentRepository.save(appointment1);
        appointmentRepository.save(appointment2);

        Assertions.assertThat(exchangeService.getEligibleAppointmentsForExchange(2)).containsOnly(appointment1);
        Mockito.verify(appointmentRepository).getOne(appointmentIdCaptor.capture());
    }

    /**
     * Realiza una prueba que mediante stubs apropiados demuestre
     * que cuando el userID es igual al userId del oldAppointment
     * se lanza una RuntimeException con mensaje Unauthorized
     */
    @Test
    void checkIfExchangeIsPossibleTest() {
        Mockito.when(appointment2.getCustomer()).thenReturn(customer1);
        // Mockito.doThrow(new RuntimeException()).when(exchangeService).checkIfExchangeIsPossible(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt());

        Assertions.assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> {
            exchangeService.checkIfExchangeIsPossible(1, 2, 1);
        });
    }

    /**
     * Crea un stub para exchangeRequestRepository.getOne
     * que devuelva un exchangeRequest que contiene una cita (Appointment)
     * en el método getRequestor.
     * Verifica que se invoca exchangeRequestRepository.save capturando
     * al exchangeRequest y comprobando que se le ha establecido un status
     * rechazado (REJECTED).
     * Verfifica se invoca al método con el exchangeRequest del stub.
     */
    @Test
    void rejectExchangeTest() {
        ExchangeRequest er = Mockito.spy(new ExchangeRequest());
        er.setId(1);
        er.setRequestor(appointment1);

        Mockito.when(exchangeRequestRepository.getOne(Mockito.anyInt())).thenReturn(er);

        ArgumentCaptor<ExchangeRequest> exchangeCaptor = ArgumentCaptor.forClass(ExchangeRequest.class);
        exchangeService.rejectExchange(1);

        InOrder inOrder = Mockito.inOrder(exchangeRequestRepository, er);
        inOrder.verify(er).setStatus(ExchangeStatus.REJECTED);
        inOrder.verify(exchangeRequestRepository).save(exchangeCaptor.capture());
    }
}
