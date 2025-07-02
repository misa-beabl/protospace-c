package in.tech_camp.protospace_c.repository;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import in.tech_camp.protospace_c.entity.DirectMessageEntity;

@Mapper
public interface DirectMessageRepository {
  @Insert("INSERT INTO direct_messages (text, sender_user_id, receiver_user_id) VALUES (#{text}, #{senderUser.id}, #{receiverUser.id})")
  @Options(useGeneratedKeys = true, keyProperty = "id")
  void insert(DirectMessageEntity directMessage);

  @Select("SELECT * FROM direct_messages ORDER BY sent_at DESC")
  @Results({
      @Result(property = "id", column = "id"),
      @Result(property = "text", column = "text"),
      @Result(property = "senderUser", column = "sender_user_id",
              one = @One(select = "in.tech_camp.protospace_c.repository.UserRepository.findById")),
      @Result(property = "receiverUser", column = "receiver_user_id",
              one = @One(select = "in.tech_camp.protospace_c.repository.UserRepository.findById")),
      @Result(property = "sentAt", column = "sent_at")
  })
  List<DirectMessageEntity> findAll();

  @Select("SELECT * FROM direct_messages WHERE id = #{id}")
  @Results({
      @Result(property = "id", column = "id"),
      @Result(property = "text", column = "text"),
      @Result(property = "senderUser", column = "sender_user_id",
              one = @One(select = "in.tech_camp.protospace_c.repository.UserRepository.findById")),
      @Result(property = "receiverUser", column = "receiver_user_id",
              one = @One(select = "in.tech_camp.protospace_c.repository.UserRepository.findById")),
      @Result(property = "sentAt", column = "sent_at")
  })
  DirectMessageEntity findById(Integer id);

  @Select("SELECT * FROM direct_messages WHERE sender_user_id = #{senderUserId} ORDER BY sent_at DESC")
  @Results({
      @Result(property = "id", column = "id"),
      @Result(property = "text", column = "text"),
      @Result(property = "senderUser", column = "sender_user_id",
              one = @One(select = "in.tech_camp.protospace_c.repository.UserRepository.findById")),
      @Result(property = "receiverUser", column = "receiver_user_id",
              one = @One(select = "in.tech_camp.protospace_c.repository.UserRepository.findById")),
      @Result(property = "sentAt", column = "sent_at")
  })
  List<DirectMessageEntity> findAllBySenderUserId(Integer senderUserId);

  @Select("SELECT * FROM direct_messages  WHERE receiver_user_id = #{receiverUserId} ORDER BY sent_at DESC")
  @Results({
      @Result(property = "id", column = "id"),
      @Result(property = "text", column = "text"),
      @Result(property = "senderUser", column = "sender_user_id",
              one = @One(select = "in.tech_camp.protospace_c.repository.UserRepository.findById")),
      @Result(property = "receiverUser", column = "receiver_user_id",
              one = @One(select = "in.tech_camp.protospace_c.repository.UserRepository.findById")),
      @Result(property = "sentAt", column = "sent_at")
  })
  List<DirectMessageEntity> findAllByReceiverUserId(Integer receiverUserId);

  @Select("SELECT * FROM direct_messages WHERE (sender_user_id = #{userId1} AND receiver_user_id = #{userId2}) " +
          "OR (sender_user_id = #{userId2} AND receiver_user_id = #{userId1}) ORDER BY sent_at")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "text", column = "text"),
        @Result(property = "senderUser", column = "sender_user_id",
                one = @One(select = "in.tech_camp.protospace_c.repository.UserRepository.findById")),
        @Result(property = "receiverUser", column = "receiver_user_id",
                one = @One(select = "in.tech_camp.protospace_c.repository.UserRepository.findById")),
        @Result(property = "sentAt", column = "sent_at")
    })
    List<DirectMessageEntity> findBetweenUsers(Integer userId1, Integer userId2);

    @Delete("DELETE FROM direct_messages WHERE id = #{id}")
    void deleteById(Integer id);

    @Delete("DELETE FROM direct_messages WHERE " +
            "(sender_user_id = #{userId1} AND receiver_user_id = #{userId2}) " +
            "OR (sender_user_id = #{userId2} AND receiver_user_id = #{userId1})")
    void deleteBetweenUsers(Integer userId1, Integer userId2);

    @Select(
    "SELECT * FROM ("
    + "  SELECT *, "
    + "    CASE "
    + "      WHEN sender_user_id = #{userId} THEN receiver_user_id "
    + "      ELSE sender_user_id "
    + "    END AS partner_id, "
    + "    ROW_NUMBER() OVER (PARTITION BY "
    + "      (CASE WHEN sender_user_id = #{userId} THEN receiver_user_id ELSE sender_user_id END) "
    + "      ORDER BY sent_at DESC"
    + "    ) AS rn "
    + "  FROM direct_messages "
    + "  WHERE sender_user_id = #{userId} OR receiver_user_id = #{userId} "
    + ") t "
    + "WHERE rn = 1 "
    + "ORDER BY sent_at DESC "
    + "LIMIT 5"
    )
    @Results({
      @Result(property = "id", column = "id"),
      @Result(property = "text", column = "text"),
      @Result(property = "senderUser", column = "sender_user_id",
              one = @One(select = "in.tech_camp.protospace_c.repository.UserRepository.findById")),
      @Result(property = "receiverUser", column = "receiver_user_id",
              one = @One(select = "in.tech_camp.protospace_c.repository.UserRepository.findById")),
      @Result(property = "sentAt", column = "sent_at")
    })
    List<DirectMessageEntity> findLatestMessagesByPartner(Integer userId);
}
