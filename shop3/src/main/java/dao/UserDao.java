package dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;


import logic.User;

@Repository
public class UserDao {
	private NamedParameterJdbcTemplate template;
	RowMapper<User> mapper= new BeanPropertyRowMapper<>(User.class);
	Map<String,Object> param = new HashMap<>();
	
	@Autowired
	public void setDataSource(DataSource dataSource) {
		this.template= new NamedParameterJdbcTemplate(dataSource);
	}

	public void insert(User user) {
		param.clear();
		SqlParameterSource prop = new BeanPropertySqlParameterSource(user);
		String sql="insert into useraccount (userid,username,password,birthday,phoneno,postcode,address,email)"
				+"values(:userid,:username,:password,:birthday,:phoneno,:postcode,:address,:email)";
		template.update(sql,prop);
		
	}

	public User selectOne(String userid) {
		param.clear();
		param.put("userid", userid);
		return template.queryForObject("select * from useraccount where userid=:userid", param, mapper);
	
	}
	
	public void update(User user) {
		String sql="update useraccount set username=:username, phoneno=:phoneno, postcode=:postcode,address=:address,"
				+ "email=:email,birthday=:birthday where userid=:userid";
		SqlParameterSource prop = new BeanPropertySqlParameterSource(user);
		template.update(sql,prop);
	}

	public void delete(String userid) {
		System.out.println("userid: "+ userid);
		param.clear();
		param.put("userid", userid);
		String sql="delete from useraccount where userid=:userid";
		template.update(sql,param);
	}

	public List<User> getlistAll() {
		return template.query("select * from useraccount", mapper);
	}
}
