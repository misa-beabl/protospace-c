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
import org.apache.ibatis.annotations.Update;

import in.tech_camp.protospace_c.entity.CommentEntity;

@Mapper
public interface CommentRepository {
        @Select("SELECT c.*, u.id AS user_id, u.nickname AS user_nickname " + 
        "FROM comments c JOIN users u ON c.user_id = u.id WHERE c.prototype_id = #{prototypeId} ORDER BY c.created_at ASC")
        @Results(value = {
        @Result(property="id", column="id"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "user", column = "user_id",
                one = @One(select = "in.tech_camp.protospace_c.repository.UserRepository.findById")),
        @Result(property = "prototype", column = "prototype_id", 
                one = @One(select = "in.tech_camp.protospace_c.repository.PrototypeRepository.findById")),
        @Result(property="parentComment", column="parent_id",
                one = @One(select="in.tech_camp.protospace_c.repository.CommentRepository.findBasicById"))
        })
        List<CommentEntity> findByPrototypeId(Integer prototypeId);

        @Select("select id from comments where id=#{id}")
        CommentEntity findBasicById(Integer id);

        @Insert("INSERT INTO comments (text, user_id, prototype_id, image, parent_id) " + 
        "VALUES (#{text}, #{user.id}, #{prototype.id}, #{image}, #{parentComment.id})")
        @Options(useGeneratedKeys = true, keyProperty = "id")
        void insert(CommentEntity comment);

        @Delete("DELETE FROM comments WHERE id = #{id}")
        void deleteById(Integer id);

        @Select("SELECT c.*, u.id AS user_id, u.nickname AS user_nickname FROM comments c JOIN users u ON c.user_id = u.id WHERE c.id = #{id}")
        @Results(value = {
        @Result(property="id", column="id"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "user", column = "user_id",
                one = @One(select = "in.tech_camp.protospace_c.repository.UserRepository.findById")),
        @Result(property = "prototype", column = "prototype_id",
                one = @One(select = "in.tech_camp.protospace_c.repository.PrototypeRepository.findById"))
        })
        CommentEntity findById(Integer id);

        @Update("UPDATE comments SET text = #{text}, image = #{image} WHERE id = #{id}")
        void update(CommentEntity comment);

        // @Select("SELECT * from comments where parent_id=#{parentId}")
        // @Results(value = {
        // @Result(property="id", column="id"),
        // @Result(property = "user", column = "user_id",
        //         one = @One(select = "in.tech_camp.protospace_c.repository.UserRepository.findById")),
        // @Result(property = "prototype", column = "prototype_id",
        //         one = @One(select = "in.tech_camp.protospace_c.repository.PrototypeRepository.findById"))
        // })
        // List<CommentEntity> findByParentId(Integer parentId);
}