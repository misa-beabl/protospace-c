package in.tech_camp.protospace_c.repository;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import in.tech_camp.protospace_c.entity.PrototypeEntity;

@Mapper
public interface PrototypeRepository {

  @Select("SELECT * FROM prototypes WHERE name LIKE CONCAT('%', #{text}, '%') "
      + "or slogan LIKE CONCAT('%', #{text}, '%') "
      + "or concept LIKE CONCAT('%', #{text}, '%')")
  @Results(value = {
    @Result(property = "id", column = "id"),
    @Result(property = "user", column = "user_id",
            one = @One(select = "in.tech_camp.protospace_c.repository.UserRepository.findById")),
    @Result(property = "createdAt", column = "created_at"),
    @Result(property = "comments", column = "id", 
            many = @Many(select = "in.tech_camp.protospace_c.repository.CommentRepository.findByPrototypeId")),
    @Result(property = "genre", column = "genre_id",
            one = @One(select = "in.tech_camp.protospace_c.repository.GenreRepository.findById"))
  })
  List<PrototypeEntity> findByTextContaining(String text);


  @Insert("INSERT INTO prototypes (user_id, name, slogan, concept, image, genre_id) VALUES (#{user.id}, #{name}, #{slogan}, #{concept}, #{image}, #{genre.id})")
  @Options(useGeneratedKeys = true, keyProperty = "id")
  void insert(PrototypeEntity prototype);

  @Update("UPDATE prototypes SET user_id = #{user.id}, name = #{name}, slogan = #{slogan}, concept = #{concept}, image = #{image}, genre_id = #{genre.id} WHERE id = #{id}")
  void update(PrototypeEntity prototype);

  @Select("SELECT * FROM prototypes ORDER BY created_at DESC")
  @Results({
    @Result(property = "user", column = "user_id",
    one = @One(select = "in.tech_camp.protospace_c.repository.UserRepository.findById")),
    @Result(property = "createdAt", column = "created_at"),
    @Result(property = "likeCount", column = "like_count"),
    @Result(property = "genre", column = "genre_id",
            one = @One(select = "in.tech_camp.protospace_c.repository.GenreRepository.findById"))
  })
  List<PrototypeEntity> findAll();

  @Select("SELECT * FROM prototypes WHERE id = #{id}")
  @Results(value = {
    @Result(property = "id", column = "id"),
    @Result(property = "user", column = "user_id",
            one = @One(select = "in.tech_camp.protospace_c.repository.UserRepository.findById")),
    @Result(property = "createdAt", column = "created_at"),
    @Result(property = "likeCount", column = "like_count"),
    @Result(property = "comments", column = "id", 
            many = @Many(select = "in.tech_camp.protospace_c.repository.CommentRepository.findByPrototypeId")),
    @Result(property = "genre", column = "genre_id",
            one = @One(select = "in.tech_camp.protospace_c.repository.GenreRepository.findById"))            
  })
  PrototypeEntity findById(Integer id);

  @Select("SELECT * FROM prototypes WHERE user_id = #{userId} ORDER BY created_at DESC")
  @Results({
    @Result(property = "user", column = "user_id",
            one = @One(select = "in.tech_camp.protospace_c.repository.UserRepository.findById")),
    @Result(property = "createdAt", column = "created_at"),
    @Result(property = "likeCount", column = "like_count"),
    @Result(property = "genre", column = "genre_id",
            one = @One(select = "in.tech_camp.protospace_c.repository.GenreRepository.findById"))
  })
  List<PrototypeEntity> findByUserId(Integer userId);

  @Delete("DELETE FROM prototypes WHERE id = #{id}")
  void deleteById(Integer id);

  @Delete("DELETE FROM prototypes")
  void deleteAll();

  /** いいね数 +1 */
  @Update("UPDATE prototypes SET like_count = like_count + 1 WHERE id = #{prototypeId}")
  void incrementLikeCount(Integer prototypeId);

  /** いいね数 -1（0未満にならないようにする場合は注意） */
  @Update("UPDATE prototypes SET like_count = like_count - 1 WHERE id = #{prototypeId} AND like_count > 0")
  void decrementLikeCount(Integer prototypeId);

  @Select({
        "<script>", // SQLが動的であることをMyBatisに伝える
        "SELECT * FROM prototypes",
        "<where>", // WHERE句を動的に制御
                " <if test='genreIds != null and !genreIds.isEmpty()'>", // genreIdsが空でない場合のみ条件を追加
                        " genre_id IN", // IN 句を開始
                        " <foreach item='id' collection='genreIds' open='(' separator=',' close=')'>", // リストをループして展開
                                " #{id}", // 各ジャンルIDをSQLに埋め込む
                        " </foreach>",
                " </if>",
        "</where>",
        "</script>"
})
  @Results(value = {
    @Result(property = "id", column = "id"),
    @Result(property = "user", column = "user_id",
            one = @One(select = "in.tech_camp.protospace_c.repository.UserRepository.findById")),
    @Result(property = "createdAt", column = "created_at"),
    @Result(property = "comments", column = "id", 
            many = @Many(select = "in.tech_camp.protospace_c.repository.CommentRepository.findByPrototypeId")),
    @Result(property = "genre", column = "genre_id",
            one = @One(select = "in.tech_camp.protospace_c.repository.GenreRepository.findById"))
  })
  List<PrototypeEntity> findByGenreIdIn(@Param("genreIds") List<Long> genreIds);
}
