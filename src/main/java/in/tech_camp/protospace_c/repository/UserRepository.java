package in.tech_camp.protospace_c.repository;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import in.tech_camp.protospace_c.entity.UserEntity;

@Mapper
public interface UserRepository {
  @Insert(
  "INSERT INTO users (nickname, email, password, profile, affiliation, position, avatar) " +
  "VALUES (#{nickname}, #{email}, #{password}, #{profile}, #{affiliation}, #{position}, #{avatar})"
  )
  @Options(useGeneratedKeys = true, keyProperty = "id")
  void insert(UserEntity user);

  @Select("SELECT EXISTS(SELECT 1 FROM users WHERE email = #{email})")
  boolean existsByEmail(String email);

  @Select("SELECT * FROM users")
  List<UserEntity> findAll();

  @Select("SELECT * FROM users WHERE email = #{email}")
  UserEntity findByEmail(String email);

  @Select("SELECT * FROM users WHERE id = #{id}")
  @Results(value = {
    @Result(property = "id", column = "id"),
    @Result(property = "prototypes", column = "id", 
            many = @Many(select = "in.tech_camp.protospace_c.repository.PrototypeRepository.findByUserId"))
  })
  UserEntity findById(Integer id);

  @Delete("DELETE FROM users")
  void deleteAll();

  @Update("UPDATE users SET nickname=#{nickname}, email=#{email}, password=#{password}, profile=#{profile}, affiliation=#{affiliation}, position=#{position}, avatar=#{avatar} WHERE id=#{id}")
  void update(UserEntity user);
}
