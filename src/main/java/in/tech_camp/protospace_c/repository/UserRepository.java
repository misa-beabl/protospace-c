package in.tech_camp.protospace_c.repository;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import in.tech_camp.protospace_c.entity.UserEntity;

@Mapper
public interface UserRepository {
  @Insert(
  "INSERT INTO users (nickname, email, password, profile, affiliation, position) " +
  "VALUES (#{nickname}, #{email}, #{password}, #{profile}, #{affiliation}, #{position})"
  )
  @Options(useGeneratedKeys = true, keyProperty = "id")
  void insert(UserEntity user);

  // 用于在注册阶段检验，用户输入的邮件地址是不是已经被注册过了
  // select 1 不返回查询到的具体字段，只返回布尔，用于表示符合条件的数据是否存在
  @Select("SELECT EXISTS(SELECT 1 FROM users WHERE email = #{email})")
  boolean existsByEmail(String email);

  @Select("SELECT * FROM users")
  List<UserEntity> findAll();

  @Select("SELECT * FROM users WHERE email = #{email}")
  UserEntity findByEmail(String email);

  @Select("SELECT * FROM users WHERE id = #{id}")
  @Results(value = {
    @Result(property = "id", column = "id"),
    // 使用查询到结果中的 id 字段，带入 指定的数据库交互函数，将返回结果交给创建的用户实体的 tweets 字段
    @Result(property = "prototypes", column = "id", 
            many = @Many(select = "in.tech_camp.protospace_c.repository.PrototypeRepository.findByUserId"))
  })
  UserEntity findById(Integer id);
}
