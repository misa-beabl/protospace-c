package in.tech_camp.protospace_c.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import in.tech_camp.protospace_c.entity.GenreEntity;

@Mapper
public interface GenreRepository {
  @Select("SELECT * FROM genres")
  @Results(value = {
    @Result(property="genreName", column="genre_name")})
  List<GenreEntity> findAll();
}
