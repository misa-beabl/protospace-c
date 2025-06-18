package in.tech_camp.protospace_c.repository;

import java.util.List;

import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import in.tech_camp.protospace_c.entity.PrototypeEntity;

public interface PrototypeRepository {
  @Select("SELECT * FROM prototypes WHERE user_id = #{id}")
  @Results(value = {
  // 使用查询结果中的 user_id 字段，调用下边的查询函数，然后将返回结果绑定在创建的推文对象的 user 属性上
  @Result(property = "user", column = "user_id",
            one = @One(select = "in.tech_camp.protospace_c.repository.UserRepository.findById"))
  })
  List<PrototypeEntity> findByUserId(Integer id);
}
