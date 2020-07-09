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

import logic.Sale;
import logic.SaleItem;

@Repository
public class SaleItemDao {
	private NamedParameterJdbcTemplate template;
	private RowMapper<SaleItem> mapper= new BeanPropertyRowMapper<SaleItem>(SaleItem.class);
	private Map<String,Object> param = new HashMap<>();
	@Autowired
	public void setDataSource(DataSource dateSource) { 
		template=new NamedParameterJdbcTemplate(dateSource);
	}

	public void insert(SaleItem saleItem) {
		String sql="insert into saleitem (saleid,seq,itemid, quantity) values (:saleid,:seq,:itemid, :quantity) ";
		SqlParameterSource prop= new BeanPropertySqlParameterSource(saleItem);
		template.update(sql,prop); 
		
	}

	public List<SaleItem> list(int saleid) {	// �ֹ���ǰ���
		String sql="select * from saleitem where saleid=:saleid";
		param.clear();
		param.put("saleid",saleid);
		return template.query(sql, param ,mapper); 
	}

	

}
