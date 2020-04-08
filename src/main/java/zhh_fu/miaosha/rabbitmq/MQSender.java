package zhh_fu.miaosha.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import zhh_fu.miaosha.Util.JedisAdapter;

@Service
public class MQSender {

    private static Logger log = LoggerFactory.getLogger(MQSender.class);

    @Autowired
    AmqpTemplate amqpTemplate;

    /*
    public void sent(Object message){
        String msg = JedisAdapter.beanToString(message);
        log.info("send message :" + msg);
        amqpTemplate.convertAndSend(MQConfig.QUEUE,msg);
    }
    */

    public void sentMiaoshaMessage(MiaoshaMessage message){
        String msg = JedisAdapter.beanToString(message);
        log.info("send message :" + msg);
        amqpTemplate.convertAndSend(MQConfig.MIAOSHA_QUEUE,msg);
    }
}
