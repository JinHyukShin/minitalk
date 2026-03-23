package com.minitalk.domain.chat.repository;

import com.minitalk.domain.chat.document.Message;
import java.time.Instant;
import java.util.List;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
public class MessageRepositoryCustomImpl implements MessageRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    public MessageRepositoryCustomImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void markAsReadBulk(Long roomId, Long userId, String lastMessageId) {
        ObjectId lastMsgObjectId = new ObjectId(lastMessageId);

        Query query = Query.query(Criteria.where("roomId").is(roomId)
            .and("_id").lte(lastMsgObjectId)
            .and("readBy.userId").ne(userId));

        Update update = new Update().push("readBy",
            new Message.ReadReceipt(userId, Instant.now()));

        mongoTemplate.updateMulti(query, update, Message.class);
    }

    @Override
    public List<Message> searchMessages(Long roomId, String query, int page, int size) {
        TextCriteria textCriteria = TextCriteria.forDefaultLanguage().matching(query);

        Query mongoQuery = TextQuery.queryText(textCriteria)
            .sortByScore()
            .addCriteria(Criteria.where("roomId").is(roomId))
            .addCriteria(Criteria.where("deleted").is(false))
            .skip((long) page * size)
            .limit(size);

        return mongoTemplate.find(mongoQuery, Message.class);
    }
}
