package com.bixin.gameFi.aww.core.mapper;

import com.bixin.gameFi.aww.bean.DO.BOBMintInfo;

public interface BobMintInfoMapper {
//    long countByExample(BobMintInfoDDL example);
//
//    int deleteByExample(BobMintInfoDDL example);
//
//    int deleteByPrimaryKey(Long id);

//    int insert(BobMintInfo record);

//    int insertSelective(BobMintInfo record);

//    List<BobMintInfo> selectByExample(BobMintInfoDDL example);

    BOBMintInfo selectByPrimaryKey(Long id);
    BOBMintInfo selectByState();


//    int updateByExampleSelective(@Param("record") BobMintInfo record, @Param("example") BobMintInfoDDL example);
//
//    int updateByExample(@Param("record") BobMintInfo record, @Param("example") BobMintInfoDDL example);

    int updateByPrimaryKeySelective(BOBMintInfo record);

//    int updateByPrimaryKey(BobMintInfo record);
}