package com.milepost.service.mybatis.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.milepost.api.dto.request.PageParameter;
import com.milepost.service.mybatis.dao.BaseMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 删除了带有example参数的方法，防止开发人员在controller中写service代码
 * @param <M>
 * @param <E>
 */
@Transactional	//子类会继承父类的注解
public class BaseService<M, E> {

	@Autowired
	private BaseMapper<M, E> baseMapper;
	
	/**
	 * 根据条件查询列表数据，返回List<M>
	 * @param example 传null表示查询所有
	 * @return
	 */
//    public List<M> selectByExample(E example){
//    	return baseMapper.selectByExample(example);
//    }
    
    /**
	 * 根据id查询单个数据，返回M
	 * @param id
	 * @return
	 */
    public M selectByPrimaryKey(String id){
    	return baseMapper.selectByPrimaryKey(id);
    }

    /**
	 * 根据条件删除数据，
	 * @param example 传null表示删除所有
	 * @return
	 */
//	@Transactional
//	public int deleteByExample(E example){
//		return baseMapper.deleteByExample(example);
//	}

	/**
	 * 根据id删除数据，
	 * @param id
	 * @return
	 */
	@Transactional
	public int deleteByPrimaryKey(String id){
		return baseMapper.deleteByPrimaryKey(id);
	}

	/**
	 * 针对所有字段插入数据，为null的字段将被插入null
	 * @param record
	 * @return
	 */
	@Transactional
	public int insert(M record){
		return baseMapper.insert(record);
	}

	/**
	 * 针对非null字段插入数据，为null的字段将使用数据库默认值
	 * @param record
	 * @return
	 */
	@Transactional
	public int insertSelective(M record){
		return baseMapper.insertSelective(record);
	}

	/**
	 * 根据条件查询数据个数，
	 * @param example 传null表示查询所有
	 * @return
	 */
//	public int countByExample(E example){
//		return baseMapper.countByExample(example);
//	}

	/**
	 * 根据条件，针对非null字段更新数据，为null的字段将保留原来的值
	 * @param record
	 * @param example 传null表示更新所有
	 * @return
	 */
//    @Transactional
//    public int updateByExampleSelective(M record, E example){
//    	return baseMapper.updateByExampleSelective(record, example);
//    }

    /**
	 * 根据条件，针对所有字段更新数据，为null的字段将被赋值成null
	 * @param record
	 * @param example 传null表示更新所有
	 * @return
	 */
//    @Transactional
//    public int updateByExample(M record, E example){
//    	return baseMapper.updateByExample(record, example);
//    }

    /**
	 * 根据id，针对非null字段更新数据，为null的字段将保留原来的值
	 * @param record
	 * @return
	 */
    @Transactional
    public int updateByPrimaryKeySelective(M record){
    	return baseMapper.updateByPrimaryKeySelective(record);
    }

    /**
	 * 根据id，针对所有字段更新数据，为null的字段将被赋值成null
	 * @param record
	 * @return
	 */
    @Transactional
    public int updateByPrimaryKey(M record){
    	return baseMapper.updateByPrimaryKey(record);
    }
    
    //以上方法，是mbg生成的mapper接口中的方法，以下方法是封装的，增加了分页功能 //
    
    /**
     * 查询所有数据
     * @return
     */
    public List<M> selectAll(){
    	return baseMapper.selectByExample(null);
    }
    
    /**
	 * 根据条件查询单个数据，返回M，
	 * 如果结果集中有多余一条数据，则会抛出异常。
	 * 如果结果集中没有数据，则返回null。
	 * @param example
	 * @return
     * @throws Exception 
	 */
    public M selectOneByExample(E example) throws Exception{
    	List<M> list = baseMapper.selectByExample(example);
    	int count = list.size();
    	if(count > 1){
    		throw new Exception("Expected one result (or null), but found: " + count);
    	}else if(count == 1){
    		return list.get(0);
    	}else{
    		return null;
    	}
    }
    
    /**
	 * 根据条件和分页参数查询数据，
	 * @param example 传null表示查询所有
     * @param pageParameter 分页参数
	 * @return 返回List
	 */
//    @Deprecated
//	public List<M> selectByExampleForList(E example, PageParameter pageParameter) {
//		PageHelper.startPage(pageParameter);
//		return baseMapper.selectByExample(example);
//	}
    
    /**
     * 根据条件和分页参数查询数据，
     * @param example 传null表示查询所有
     * @param pageParameter 分页参数
     * @return 返回Page对象
     */
//	public Page<M> selectByExampleForPage(E example, PageParameter pageParameter) {
//		PageHelper.startPage(pageParameter);
//		return (Page<M>)baseMapper.selectByExample(example);
//	}

	/**
	 * 根据条件和分页参数查询数据，（推荐使用这个方法来实现分页）
	 * @param example 传null表示查询所有
     * @param pageParameter 分页参数
	 * @return 返回PageInfo对象，PageInfo比Page中的分页信息丰富一些，推荐使用这个方法来实现分页
	 */
	public PageInfo<M> selectByExampleForPageInfo(E example, PageParameter pageParameter) {
		PageHelper.startPage(pageParameter);
		List<M> list = baseMapper.selectByExample(example);
		return new PageInfo<M>(list);
	}
    
}
