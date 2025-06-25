package in.tech_camp.protospace_c.repository;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface LikesRepository {
  @Insert("INSERT INTO likes (user_id, prototype_id) VALUES (#{userId}, #{prototypeId})")
  void insert(Integer userId, Integer prototypeId);

  @Delete("DELETE FROM likes WHERE user_id = #{userId} AND prototype_id = #{prototypeId}")
  void delete(Integer userId, Integer prototypeId);

  @Select("SELECT * FROM likes WHERE user_id = #{userId}")
  List<Integer> findByUserId(Integer userId);

  @Select("SELECT prototype_id FROM likes WHERE user_id = #{userId}")
  List<Integer> findLikedPrototypeIdsByUserId(Integer userId);

  @Select("SELECT EXISTS(SELECT 1 FROM likes WHERE user_id = #{userId} AND prototype_id = #{prototypeId})")
  Boolean exists(Integer userId, Integer prototypeId);

}
