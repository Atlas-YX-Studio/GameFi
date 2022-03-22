### 获取mint数据

【GET】http://ip:port/v1/aww/normal/mintInfo?account={account}

请求参数说明：

| 名称    | 类型   | 描述     |
| ------- | ------ | -------- |
| account | String | 用户地址 |

返回示例：

```
{"code":0,"msg":"","detailMsg":"","data":{"imageLink":"","name":"1","description":"1","id":1,"state":1,"account":"456"}}
```

返回参数说明：

| 名称        | 类型   | 描述     |
| ----------- | ------ | -------- |
| imageLink   | String | 图片地址 |
| name        | String | 名称     |
| id          | String | id       |
| description | String | 描述     |
| account     | String | 用户地址 |



### 查询普通门票列表

【GET】http://ip:port/v1/aww/normal/normalTicket?account={account}

请求参数说明：

| 名称    | 类型   | 描述     |
| ------- | ------ | -------- |
| account | String | 用户地址 |



返回示例：

```
{
    "code": 0,
    "msg": "",
    "detailMsg": "",
    "data": [
        {
            "image": "https://imagedelivery.net/3mRLd_IbBrrQFSP57PNsVw/b49293ea-daed-401e-8ee6-27fa5e147d00/public",
            "creator": "0x88637dda61b276f8170de63f6422f7c5",
            "name": "5ace91b7-7bf0-4bb2-8f16-5f79ac62cb42",
            "description": "5ace91b7-7bf0-4bb2-8f16-5f79ac62cb42",
            "id": 4,
            "used": false
        },
        {
            "image": "https://imagedelivery.net/3mRLd_IbBrrQFSP57PNsVw/b49293ea-daed-401e-8ee6-27fa5e147d00/public",
            "creator": "0x88637dda61b276f8170de63f6422f7c5",
            "name": "c63edaeb-8340-4c80-ad1e-b641595b7b78",
            "description": "c63edaeb-8340-4c80-ad1e-b641595b7b78",
            "id": 5,
            "used": false
        }
    ]
}
```

返回参数说明：

| 名称        | 类型   | 描述     |
| ----------- | ------ | -------- |
| data        | arry   | 门票列表 |
| id          | String | 门票id   |
| image       | String | 图片地址 |
| name        | String | 门票名称 |
| description | String | 描述     |



### 查询普通场竞赛信息接口

【GET】http://ip:port/v1/aww/normal/raceInfo?account={account}

请求参数说明：

| 名称    | 类型   | 描述         |
| ------- | ------ | ------------ |
| account | String | 当前用户地址 |



返回示例：

```
{
    "code": 0,
    "msg": "",
    "detailMsg": "",
    "data": {
        "items": [
            {
                "owner": "0x88637dda61b276f8170de63f6422f7c5",
                "image": "https://imagedelivery.net/3mRLd_IbBrrQFSP57PNsVw/b49293ea-daed-401e-8ee6-27fa5e147d00/public",
                "creator": "0x88637dda61b276f8170de63f6422f7c5",
                "name": "d38fc32a-41e1-49fe-b5b3-4141f62c6577",
                "description": "d38fc32a-41e1-49fe-b5b3-4141f62c6577",
                "id": "1",
                "used": false
            },
            {
                "owner": "0x88637dda61b276f8170de63f6422f7c5",
                "image": "https://imagedelivery.net/3mRLd_IbBrrQFSP57PNsVw/b49293ea-daed-401e-8ee6-27fa5e147d00/public",
                "creator": "0x88637dda61b276f8170de63f6422f7c5",
                "name": "c5993a90-da21-4a1d-9a36-1c59c632102f",
                "description": "c5993a90-da21-4a1d-9a36-1c59c632102f",
                "id": "2",
                "used": false
            },
            {
                "owner": "0x88637dda61b276f8170de63f6422f7c5",
                "image": "https://imagedelivery.net/3mRLd_IbBrrQFSP57PNsVw/b49293ea-daed-401e-8ee6-27fa5e147d00/public",
                "creator": "0x88637dda61b276f8170de63f6422f7c5",
                "name": "aad64705-b60f-4112-a094-cad7a9c6d180",
                "description": "aad64705-b60f-4112-a094-cad7a9c6d180",
                "id": "3",
                "used": false
            }
        ],
        "name":"name",
        "img":"https://imagedelivery.net/3mRLd_IbBrrQFSP57PNsVw/b49293ea-daed-401e-8ee6-27fa5e147d00/public",
        "total_reward": 700,
        "total_reward_token": {
            "addr": "0x00000000000000000000000000000001",
            "module_name": "STC",
            "name": "STC"
        },
        "surplus_reward": {
            "value": 0
        },
        "sign_up_start_ts": 1647825079000,
        "sign_up_end_ts": 1647872734989,
        "race_start_ts": 1647872734989,
        "total_round": 2,
        "current_round": 2,
        "sign_up_count": 3,
        "exit_count": 0,
        "die_count": 2,
        "actual_surplus_count": 3,
        "target_surplus_count": 1,
        "eliminate_interval": 120000,
        "next_eliminate_ts": 1647873094989,
        "eliminate_rate": 10,
        "state": 3,
        "normalRaceContract": "0x9b996121ea29b50c6213558e34120e5c::BOBNormalRaceV2::RaceInfo",
        "normalTicketMeta": "0x9b996121ea29b50c6213558e34120e5c::BOBNormalTicketV3",
        "normalTicketMBody": "0x9b996121ea29b50c6213558e34120e5c::BOBNormalTicketV3"
    }
}
```

参数说明：

| 名称                 | 类型          | 描述                                                         |
| -------------------- | ------------- | ------------------------------------------------------------ |
| total_reward         | int           | 总奖池                                                       |
| items                | 已报名nft数据 | 报名中和竞赛中（state==1 \|\| state==2）时才返回，返回当前用户的nft数据。其他情况不返回items |
| total_reward_token   | obj           | 总奖池token                                                  |
| surplus_reward       | obj           | 剩余奖池                                                     |
| sign_up_start_ts     | timestamp     | 报名开始时间                                                 |
| sign_up_end_ts       | timestamp     | 报名截止时间                                                 |
| race_start_ts        | int           | 竞赛开始时间                                                 |
| total_round          | int           | 总轮次                                                       |
| current_round        | int           | 当前轮次                                                     |
| sign_up_count        | int           | 报名人数                                                     |
| exit_count           | int           | 主动退出人数                                                 |
| die_count            | int           | 累计淘汰人数                                                 |
| actual_surplus_count | int           | 实际剩余人数                                                 |
| target_surplus_count | int           | 目标剩余人数                                                 |
| eliminate_interval   | int           | 淘汰间隔（ms）                                               |
| next_eliminate_ts    | timestamp     | 下次淘汰事件                                                 |
| eliminate_rate       | int           | 淘汰率                                                       |
| state                | int           | 状态：0：未知；1：报名中；2：竞赛中；3已结束                 |
| normalRaceContract   | string        | 合约                                                         |
| normalTicketMeta     | sting         |                                                              |
| normalTicketMBody    | string        |                                                              |
| name                 | string        | 竞赛名称                                                     |
| img                  | string        | 竞赛图片                                                     |
