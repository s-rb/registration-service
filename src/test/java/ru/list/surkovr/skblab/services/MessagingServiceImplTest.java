package ru.list.surkovr.skblab.services;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import ru.list.surkovr.skblab.model.Message;
import ru.list.surkovr.skblab.model.MessageId;
import ru.list.surkovr.skblab.services.interfaces.OuterCheckingService;

import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
public class MessagingServiceImplTest {

    public static final int TIMEOUT_TO_TEST_RECEIVE = 1000;
    @InjectMocks
    private MessagingServiceImpl messagingService;
    @Mock
    private OuterCheckingService outerCheckingService;
    @Mock
    private Map<MessageId, Boolean> msgCheckMap;

    @Test
    public void testSend_success() {
        Message<String> msg = new Message<>("Some message here");
        given(outerCheckingService.checkMessage(msg)).willReturn(true);
        MessageId id = new MessageId(UUID.randomUUID());
        var actual = messagingService.send(msg);
        assertNotNull(actual);
        assertThat(actual).isExactlyInstanceOf(MessageId.class);
    }

    @Test
    public void testSend_outerSrvcThrowsException() {
        Message<String> msg = new Message<>("Some message here");
        given(outerCheckingService.checkMessage(msg)).willThrow(UncheckedIOException.class);
        MessageId id = new MessageId(UUID.randomUUID());
        var actual = messagingService.send(msg);
        assertNull(actual);
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testReceive_success() throws Exception {
        ExecutorService pool = Executors.newSingleThreadExecutor();
        MessageId id = new MessageId(UUID.randomUUID());
        Message<Boolean> answerMsg;
        try {
            Map<MessageId, Boolean> mockMap = new HashMap<>();
            mockMap.put(id, true);
            ReflectionTestUtils.setField(messagingService, "msgCheckMap", mockMap);
            Future<Message<Boolean>> f = pool.submit(
                    () -> {
                        try {
                            return messagingService.receive(id, Boolean.class);
                        } catch (TimeoutException e) {
                            return null;
                        }
                    });
            // Блокируем текущий поток и ждем получения результата. В случае эксепшн или если поток спит,
            // выбрасываем исключение по таймауту, ловим и проверяем
            answerMsg = f.get(TIMEOUT_TO_TEST_RECEIVE, TimeUnit.MILLISECONDS);
            if (answerMsg == null) throw new TimeoutException("Timeout!");
        } catch (TimeoutException e) {
            exceptionRule.expect(TimeoutException.class);
            exceptionRule.expectMessage("Timeout!");
            return;
        }
        assertNotNull(answerMsg);
        assertThat(answerMsg).isExactlyInstanceOf(Message.class);
        assertEquals(answerMsg.getId(), id);
        assertThat(answerMsg.getData()).isNotNull();
        assertThat(answerMsg.getData()).isExactlyInstanceOf(Boolean.class);
    }
}
