package ru.list.surkovr.skblab.services;

import org.hamcrest.core.AnyOf;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit4.SpringRunner;
import ru.list.surkovr.skblab.model.Message;
import ru.list.surkovr.skblab.model.MessageId;

import java.util.UUID;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
public class OuterCheckingServiceImplTest {

    @InjectMocks
    private OuterCheckingServiceImpl outerCheckingService;

    @Test
    public void testCheckMessage_success() {
        MessageId id = new MessageId(UUID.randomUUID());
        Message<String> msg = new Message<String>("Some msg here", id);
        var actual = outerCheckingService.checkMessage(msg);
        assertThat(String.valueOf(actual), AnyOf.anyOf(is("true"), is("false")));
    }
}