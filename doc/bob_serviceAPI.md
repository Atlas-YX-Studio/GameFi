### 获取mint数据

【GET】http://ip:port/v1/aww/normal/mintInfo?account={account}

请求参数说明：

| 名称      | 类型     | 描述   |
| ------- | ------ | ---- |
| account | String | 用户地址 |

返回示例：

```
{"code":0,"msg":"","detailMsg":"","data":{"imageLink":"","name":"1","description":"1","id":1,"state":1,"account":"456"}}
```

返回参数说明：

| 名称          | 类型     | 描述   |
| ----------- | ------ | ---- |
| imageLink   | String | 图片地址 |
| name        | String | 名称   |
| id          | String | id   |
| description | String | 描述   |
| account     | String | 用户地址 |



### 查询普通门票列表

【GET】http://ip:port/v1/aww/normal/normalTicket?account={account}&raceType=normal

请求参数说明：

| 名称       | 类型     | 描述                           |
| -------- | ------ | ---------------------------- |
| account  | String | 用户地址                         |
| raceType | String | 当前竞赛类型：普通场：normal；高级场：senior |



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

| 名称          | 类型     | 描述   |
| ----------- | ------ | ---- |
| data        | arry   | 门票列表 |
| id          | String | 门票id |
| image       | String | 图片地址 |
| name        | String | 门票名称 |
| description | String | 描述   |



### 查询普通场竞赛信息接口

【GET】http://ip:port/v1/aww/normal/raceInfo?account={account}

请求参数说明：

| 名称      | 类型     | 描述     |
| ------- | ------ | ------ |
| account | String | 当前用户地址 |



返回示例：

```
{
    "code": 0,
    "msg": "",
    "detailMsg": "",
    "data": {
    	"raceType":"normal",
        "name": "name",
        "img": "https://imagedelivery.net/3mRLd_IbBrrQFSP57PNsVw/0659d41a-26c5-474d-88a3-491575c00700/public",
        "sign_up_limit": 50,
        "person_info": [],
        "platform_manager_fee": 0,
        "super_prize_fee": 0,
        "prize_pool_fee": 0,
        "total_reward": 0,
        "total_reward_token": {
            "addr": "0x00000000000000000000000000000001",
            "module_name": "STC",
            "name": "STC"
        },
        "surplus_reward": {
            "value": 0
        },
        "sign_up_start_block": 4880989,
        "sign_up_end_block": 49790280,
        "signUpStartInterval":5000,
        "signUpEndInterval":5000,
        "race_start_block": 49790280,
        "total_round": 0,
        "current_round": 0,
        "sign_up_count": 0,
        "exit_count": 0,
        "die_count": 0,
        "target_surplus_count": 0,
        "eliminate_rate": 1,
        "eliminate_interval_block": 20,
        "next_eliminate_count": 0,
        "next_eliminate_block": 49790280,
        "state": 0,
        "champion_address": "0x00000000000000000000000000000000",
        "champion_nft_id": 0,
        "champion_nft_img": "",
        "second_address": "0x00000000000000000000000000000000",
        "second_nft_id": 0,
        "second_nft_img": "0x",
        "third_address": "0x00000000000000000000000000000000",
        "third_nft_id": 0,
        "third_nft_img": "0x",
        "test": [],
        "sign_up_event": {
            "counter": 0,
            "guid": "0x95000000000000009b996121ea29b50c6213558e34120e5c"
        },
        "exit_event": {
            "counter": 0,
            "guid": "0x96000000000000009b996121ea29b50c6213558e34120e5c"
        },
        "eliminate_event": {
            "counter": 0,
            "guid": "0x97000000000000009b996121ea29b50c6213558e34120e5c"
        },
        "normalRaceContract": "0x9b996121ea29b50c6213558e34120e5c::BOBNormalRaceV1003",
        "normalTicketMeta": "0x9b996121ea29b50c6213558e34120e5c::BOBNormalTicketV1003",
        "normalTicketMBody": "0x9b996121ea29b50c6213558e34120e5c::BOBNormalTicketV1003"
    }
}
```

参数说明：

| 名称                       | 类型        | 描述                                       |
| ------------------------ | --------- | ---------------------------------------- |
| total_reward             | int       | 总奖池                                      |
| items                    | 已报名nft数据  | 报名中和竞赛中（state==1 \|\| state==2）时才返回，返回当前用户的nft数据。其他情况不返回items |
| total_reward_token       | obj       | 总奖池token                                 |
| surplus_reward           | obj       | 剩余奖池                                     |
| sign_up_start_block      | timestamp | 报名开始区块                                   |
| sign_up_end_block        | timestamp | 报名截止区块                                   |
| race_start_block         | int       | 竞赛开始区块                                   |
| total_round              | int       | 总轮次                                      |
| current_round            | int       | 当前轮次                                     |
| sign_up_count            | int       | 报名人数                                     |
| exit_count               | int       | 主动退出人数                                   |
| die_count                | int       | 累计淘汰人数                                   |
| actual_surplus_count     | int       | 实际剩余人数                                   |
| target_surplus_count     | int       | 目标剩余人数                                   |
| eliminate_interval_block | int       | 淘汰间隔（区块）                                 |
| next_eliminate_block     | timestamp | 下次淘汰区块                                   |
| eliminate_rate           | int       | 淘汰率                                      |
| state                    | int       | 状态：0：未知；1：报名中；2：竞赛中；3已结束                 |
| normalRaceContract       | string    | 合约                                       |
| normalTicketMeta         | sting     |                                          |
| normalTicketMBody        | string    |                                          |
| name                     | string    | 竞赛名称                                     |
| img                      | string    | 竞赛图片                                     |
| signUpStartInterval      | Int       | 距离报名开始时间间隔（ms）,如果是超过了开始时间返回-1            |
| signUpStartInterval      | Int       | 距离报名结束时间间隔（ms）,如果是超过了结束时间返回-1            |
| raceType                 | String    | 当前竞赛类型：“normal”：普通场；“Senior”:高级场         |

### 查询被淘汰的NFT列表

【GET】http://ip:port/v1/aww/normal/fallen?account={account}

请求参数说明：

| 名称      | 类型     | 描述   |
| ------- | ------ | ---- |
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

| 名称          | 类型     | 描述   |
| ----------- | ------ | ---- |
| data        | arry   | 门票列表 |
| id          | String | 门票id |
| image       | String | 图片地址 |
| name        | String | 门票名称 |
| description | String | 描述   |



### 查询已报名、已退赛、未参赛NFT列表（MyNFT页面）

【GET】http://ip:port/v1/aww/normal/mySignedNFT?account={account}

请求参数说明：

| 名称      | 类型     | 描述   |
| ------- | ------ | ---- |
| account | String | 用户地址 |



返回示例：

```
{
    "code": 0,
    "msg": "",
    "detailMsg": "",
    "data": {
    	"champion": {
            "image": "https://test.armwrestwar.com/v1/aww/normal/getImage/1382",
            "address": "0x0474ba6aad9bed0017f60578d1b7c4e3",
            "nftId": "5"
        }
        "signArry": [
            {
                "owner": "0x93bfd6328ca4d77211c3103bf94275e6",
                "image": "https://imagedelivery.net/3mRLd_IbBrrQFSP57PNsVw/c763e501-110b-4717-ba75-fde6569fb100/public",
                "creator": "0x93bfd6328ca4d77211c3103bf94275e6",
                "name": "\u0005\u0016",
                "description": "\u0005\u0016",
                "id": "4",
                "state": 1
            }
        ],
        "endArry": [],
        "ticketArry": [
            {
                "image": "https://imagedelivery.net/3mRLd_IbBrrQFSP57PNsVw/b49293ea-daed-401e-8ee6-27fa5e147d00/public",
                "creator": "0x93bfd6328ca4d77211c3103bf94275e6",
                "name": "c76b79d0-17bf-4fdd-ab84-d89b72d8d74c",
                "description": "c76b79d0-17bf-4fdd-ab84-d89b72d8d74c",
                "id": 2,
                "state": 3
            }
        ]
    }
}
```

返回参数说明：

| 名称          | 类型     | 描述   |
| ----------- | ------ | ---- |
| data        | arry   | 门票列表 |
| id          | String | 门票id |
| image       | String | 图片地址 |
| name        | String | 门票名称 |
| description | String | 描述   |
| Signarry    | Arry   | 已报名  |
| endarry     | arry   | 已退出  |
| ticketArry  | arry   | 未报名  |
| champion    | object | 冠军数据 |
|             |        |      |

### 查询其他类型NFT列表

【GET】http://ip:port/v1/aww/normal/otherNFT?account={account}

请求参数说明：

| 名称      | 类型     | 描述   |
| ------- | ------ | ---- |
| account | String | 用户地址 |



返回示例：

```
{
    "code": 0,
    "msg": "",
    "detailMsg": "",
    "data": [
        {
            "NFTMEeta": "0x7D6409B9974e68f969c92554422cA19b::ARM2::ARMMeta",
            "NFTBody": "0x7D6409B9974e68f969c92554422cA19b::ARM2::ARMMeta",
            "image": "https://imagedelivery.net/3mRLd_IbBrrQFSP57PNsVw/0659d41a-26c5-474d-88a3-491575c00700/public",
            "creator": "0x7d6409b9974e68f969c92554422ca19b",
            "name": "2",
            "description": "2",
            "id": 2,
            "type_meta": {
                "stamina": 1,
                "win_rate_bonus": 1,
                "rarity": 1
            },
            "body": {
                "used_stamina": 0,
                "time": 0
            },
            "payToken": "0x1::STC::STC"
        }
    ]
}
```

返回参数说明：

| 名称          | 类型     | 描述   |
| ----------- | ------ | ---- |
| data        | arry   | 门票列表 |
| id          | String | 门票id |
| image       | String | 图片地址 |
| name        | String | 门票名称 |
| description | String | 描述   |
|             | Arry   | 已报名  |
|             | arry   | 已退出  |
|             | arry   | 未报名  |