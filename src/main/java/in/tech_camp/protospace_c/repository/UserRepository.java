package in.tech_camp.protospace_c.repository;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
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
}
