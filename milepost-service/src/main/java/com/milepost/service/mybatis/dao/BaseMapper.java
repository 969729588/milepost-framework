package com.milepost.service.mybatis.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

/**
 * 手动建立的与 mbg生成的 sql语句映射文件 对应的 mapper接口 都继承这个接口，
 * 泛型M要传入mbg生成的Entity，
 * 泛型E要传入mbg生成的EntityExample
 * 
 * @author Huarf
 * 2017年6月2日
 */
public interface BaseMapper<M, E> {
	
	/**
	 * 根据条件查询数据，
	 * @param example 传null表示查询所有
	 * @return
	 */
	public List<M> selectByExample(E example);

	/**
	 * 根据id查询数据，
	 * @param id
	 * @return
	 */
	public M selectByPrimaryKey(String id);

	/**
	 * 根据条件删除数据，
	 * @param example 传null表示删除所有
	 * @return
	 */
	public int deleteByExample(E example);

	/**
	 * 根据id删除数据，
	 * @param id
	 * @return
	 */
	public int deleteByPrimaryKey(String id);

	/**
	 * 针对所有字段插入数据，为null的字段将被插入null
	 * @param record
	 * @return
	 */
	public int insert(M record);

	/**
	 * 针对非null字段插入数据，为null的字段将使用数据库默认值
	 * @param record
	 * @return
	 */
	public int insertSelective(M record);

	/**
	 * 根据条件查询数据个数，
	 * @param example 传null表示查询所有
	 * @return
	 */
	public int countByExample(E example);

	/**
	 * 根据条件，针对非null字段更新数据，为null的字段将保留原来的值
	 * @param record
	 * @param example 传null表示更新所有
	 * @return
	 */
	public int updateByExampleSelective(@Param("record") M record, @Param("example") E example);

	/**
	 * 根据条件，针对所有字段更新数据，为null的字段将被赋值成null
	 * @param record
	 * @param example 传null表示更新所有
	 * @return
	 */
	public int updateByExample(@Param("record") M record, @Param("example") E example);

	/**
	 * 根据id，针对非null字段更新数据，为null的字段将保留原来的值
	 * @param record
	 * @return
	 */
	public int updateByPrimaryKeySelective(M record);

	/**
	 * 根据id，针对所有字段更新数据，为null的字段将被赋值成null
	 * @param record
	 * @return
	 */
	public int updateByPrimaryKey(M record);
    
}
