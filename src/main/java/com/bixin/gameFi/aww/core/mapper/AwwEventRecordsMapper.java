package com.bixin.gameFi.aww.core.mapper;

import com.bixin.gameFi.aww.bean.DO.AwwEventRecords;
import com.bixin.gameFi.aww.core.wrapDDL.AwwEventRecordsDDL;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface AwwEventRecordsMapper {
    long countByDDL(AwwEventRecordsDDL example);

    int deleteByDDL(AwwEventRecordsDDL example);

    int deleteByPrimaryKey(Long id);

    int insert(AwwEventRecords record);

    int insertSelective(AwwEventRecords record);

    List<AwwEventRecords> selectByDDL(AwwEventRecordsDDL example);

    AwwEventRecords selectByPrimaryKey(Long id);

    int updateByDDLSelective(@Param("record") AwwEventRecords record, @Param("example") AwwEventRecordsDDL example);

    int updateByDDL(@Param("record") AwwEventRecords record, @Param("example") AwwEventRecordsDDL example);

    int updateByPrimaryKeySelective(AwwEventRecords record);

    int updateByPrimaryKey(AwwEventRecords record);
}