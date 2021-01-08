package com.julun.huanque.common.net.services

import com.alibaba.fastjson.JSONObject
import com.julun.huanque.common.basic.Root
import com.julun.huanque.common.basic.VoidResult
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.bean.forms.*
import com.julun.huanque.common.database.table.Session
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.Body
import retrofit2.http.POST

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/6/23 10:11
 *
 *@Description: UserService
 * 测试网络接口
 *
 */
interface UserService {


    /************************************** 个人中心 **********************************************/

    /**
     * 查询用户基本信息
     */
    @POST("user/acct/info/myInfo")
    suspend fun queryUserDetailInfo(@Body form: EmptyForm = EmptyForm()): Root<UserDetailInfo>

    /**
     * 查询用户等级信息
     */
    @POST("user/acct/info/levelInfo")
    fun queryUserLevelInfoBasic(@Body form: SessionForm): Observable<Root<UserLevelInfo>>


    /**
     * 手机号登录
     */
    @POST("user/acct/login/mobile")
    suspend fun mobileLogin(@Body form: MobileLoginForm): Root<Session>

    /**
     * 微信登录
     */
    @POST("user/acct/login/weixin")
    suspend fun weiXinLogin(@Body form: WeiXinForm): Root<Session>


    /**
     * 退出登录
     */
    @POST("user/acct/login/logout")
    suspend fun logout(@Body form: EmptyForm = EmptyForm()): Root<VoidResult>

    /**
     * 手机号一键登录
     */
    @POST("user/acct/login/mobileQuick")
    suspend fun mobileQuick(@Body form: MobileQuickForm): Root<Session>

    /**
     * 更新用户数据
     */
    @POST("user/acct/data/initInfo")
    suspend fun updateInformation(@Body form: UpdateInformationForm): Root<UpdateSexBean>

    /**
     * 开始获取验证码
     */
    @POST("user/acct/login/getSmsCode")
    suspend fun startGetValidCode(@Body form: GetValidCode): Root<JSONObject>

    /**
     * 同意协议
     */
    @POST("user/acct/info/agreement")
    suspend fun agreement(@Body form: AgreementResultForm): Root<VoidResult>

//    /**
//     * 修改头像
//     */
//    @POST("user/acct/data/initHeadPic")
//    suspend fun updateHeadPic(@Body form: UpdateHeadForm): Root<UpdateHeaderBean>

    /**
     * 举报
     */
    @POST("social/friend/relation/report")
    suspend fun report(@Body form: ReportForm): Root<VoidResult>

    /**
     * 举报
     */
    @POST("social/friend/relation/reportTypeList")
    suspend fun reportTypeList(): Root<ArrayList<ManagerInfo>>

    /**
     * 获取语音文本提示信息
     */
    @POST("user/acct/data/getVoiceSignPoint")
    suspend fun getVoiceSignPoint(@Body form: EmptyForm = EmptyForm()): Root<VoiceSignPointBean>

    /**
     * 修改语音签名
     */
    @POST("user/acct/data/updateVoice")
    suspend fun updateVoice(@Body form: UpdateVoiceForm): Root<InfoPerfection>

    @POST("user/acct/data/checkNickName")
    suspend fun checkNickName(@Body form: NicknameForm): Root<VoidResult>


    /**
     * 绑定手机
     */
    @POST("user/acct/info/bindMobile")
    suspend fun bindMobile(@Body form: BindPhoneForm): Root<VoidResult>

    /**
     * 获取支付宝登录授权信息
     */
    @POST("user/acct/login/alipayAuthInfo")
    suspend fun getAlipayAuthInfo(@Body form: EmptyForm = EmptyForm()): Root<AliAuthInfo>


    /**
     * 绑定支付宝
     */
    @POST("user/acct/info/bindAlipay")
    suspend fun bindAliPay(@Body form: BindForm): Root<BindResultBean>

    /**
     * 绑定微信
     */
    @POST("user/acct/info/bindWeixin")
    suspend fun bindWeiXin(@Body form: BindForm): Root<BindResultBean>

    /**
     * 保存用户地址
     */
    @POST("user/acct/data/saveLocation")
    suspend fun saveLocation(@Body form: SaveLocationForm): Root<VoidResult>

    /**
     * 成为主播的条件检查
     */
    @POST("user/acct/info/checkToAnchor")
    suspend fun checkToAnchor(@Body form: EmptyForm = EmptyForm()): Root<VoidResult>

    /**
     * 账号与安全
     */
    @POST("user/app/security")
    suspend fun security(@Body form: EmptyForm = EmptyForm()): Root<UserSecurityInfo>


    /**
     * 获取账户余额
     */
    @POST("user/acct/info/beans")
    suspend fun beans(@Body form: EmptyForm = EmptyForm()): Root<BeansData>


    /**
     * 注销账号
     */
    @POST("user/acct/login/destroyAccount")
    suspend fun destroyAccount(@Body form: EmptyForm = EmptyForm()): Root<VoidResult>

    /**
     * 注销前检测
     */
    @POST("user/acct/login/checkDestroy")
    suspend fun checkDestroy(@Body form: EmptyForm = EmptyForm()): Root<VoidResult>

    /**
     * 注销账号
     */
    @POST("user/acct/login/destroyAccountV1")
    suspend fun destroyAccountV1(@Body form: EmptyForm = EmptyForm()): Root<VoidResult>

    /**
     *获取气泡配置
     */
    @POST("user/app/settings")
    suspend fun settings(@Body form: EmptyForm = EmptyForm()): Root<SettingBean>

    /**
     * 激活APP
     */
    @POST("user/app/start")
    suspend fun appStart(@Body form: EmptyForm = EmptyForm()): Root<VoidResult>

    /**
     * 更新性别
     */
    @POST("/user/acct/data/initInfoBySex")
    suspend fun initInfoBySex(@Body form: SexForm): Root<UpdateSexBean>

    /**
     * 更新用户名片
     */
    @POST("user/acct/data/updateCard")
    suspend fun updateCard(@Body form: UpdateInformationForm): Root<UpdateSexBean>

    /**
     * 新手礼包
     */
    @POST("user/welfare/newUserBag")
    suspend fun newUserBag(@Body form: EmptyForm = EmptyForm()): Root<NewUserGiftBean>

    /**
     * 领取新手礼包
     */
    @POST("user/welfare/receiveNewUserBag")
    suspend fun receiveNewUserBag(@Body form: EmptyForm = EmptyForm()): Root<VoidResult>

    /**
     * 分享成功
     */
    @POST("user/share/shareLog")
    fun shareLog(@Body form: ShareForm): Observable<Root<VoidResult>>

    /**
     * 检查协议是否同意
     */
    @POST("user/acct/info/checkProtocol")
    suspend fun checkProtocol(@Body form: CheckProtocolForm): Root<AgreementData>

    /**
     * 修改在线状态
     */
    @POST("user/acct/info/onlineStatus")
    suspend fun updateOnlineStatus(@Body form: LineStatusForm): Root<VoidResult>

    /**
     * 修改在线状态
     */
    @POST("user/task/saveTeachVideo")
    suspend fun saveTeachVideo(@Body form: SaveTeachVideoForm): Root<VoidResult>

    /**
     * 隐藏位置状态
     */
    @POST("user/acct/data/hideLocation")
    suspend fun hideLocation(@Body form: EmptyForm = EmptyForm()): Root<StatusForm>

    /**
     * 报错隐藏位置状态
     */
    @POST("user/acct/data/saveHideLocation")
    suspend fun saveHideLocation(@Body form: StatusForm): Root<VoidResult>

    /**
     * 获取首充数据
     */
    @POST("user/welfare/firstChargeBag")
    suspend fun firstChargeBag(@Body form: EmptyForm = EmptyForm()): Root<FirstRechargeInfo>

    /**
     * 主页信息
     */
    @POST("user/acct/info/homePage")
    suspend fun homeInfo(@Body form: UserIdForm): Root<HomePageInfo>

    /**
     * 分身账号列表
     */
    @POST("user/acct/sub/list")
    suspend fun subList(@Body form: EmptyForm = EmptyForm()): Root<AccountBean>

    /**
     * 创建分身
     */
    @POST("user/acct/sub/create")
    suspend fun subCreate(@Body form: CreateAccountForm): Root<VoidResult>

    /**
     * 分身账号登录
     */
    @POST("user/acct/login/subAccount")
    suspend fun loginSubAccount(@Body body: UserIdForm): Root<Session>

    /**
     * 保存社交意愿
     */
    @POST("user/acct/data/wish")
    suspend fun socialWish(@Body body: SocialWishIdForm): Root<VoidResult>

    /**
     * 编辑资料页面，基础信息
     */
    @POST("user/acct/info/editBasic")
    suspend fun editBasic(@Body form: EmptyForm = EmptyForm()): Root<EditPagerInfo>

    /**
     * 更新用户数据
     */
    @POST("user/acct/data/update")
    suspend fun updateUserInfo(@Body form: UpdateUserInfoForm): Root<UserProcessBean>

    /**
     * 家乡初始化
     */
    @POST("user/acct/data/initHomeTown")
    suspend fun initHomeTown(@Body form: HomeTownVersionForm): Root<EditHomeTownBean>

    /**
     * 获取城市人文
     */
    @POST("user/acct/data/getCityCulture")
    suspend fun getCityCulture(@Body from: CityIdForm): Root<EditHomeTownBean>

    /**
     * 保存城市  吃过的美食，去过的景点
     */
    @POST("user/acct/data/saveHomeTown")
    suspend fun saveHomeTown(@Body form: CultureUpdateForm): Root<UserProcessBean>

    /**
     * 星座详情
     */
    @POST("user/acct/card/constellation")
    suspend fun constellation(@Body form: ConstellationForm): Root<ConstellationInfo>

    /**
     * 学校初始化接口
     */
    @POST("user/acct/data/initSchool")
    suspend fun initSchool(@Body form: EmptyForm = EmptyForm()): Root<SchoolBean>

    /**
     * 模糊搜索学校
     */
    @POST("user/acct/data/getSchool")
    suspend fun getSchool(@Body form: SchoolForm): Root<QuerySchoolBean>

    /**
     * 保存学校数据
     */
    @POST("user/acct/data/saveSchool")
    suspend fun saveSchool(@Body form: SaveSchoolForm): Root<UserProcessBean>

    /**
     * 初始化职业数据
     */
    @POST("user/acct/data/initProfession")
    suspend fun initProfession(@Body form: EmptyForm = EmptyForm()): Root<EditProfessionBean>

    /**
     * 保存职业数据
     */
    @POST("user/acct/data/saveProfession")
    suspend fun saveProfession(@Body form: SaveProfessionForm): Root<UserProcessBean>

    /**
     * 更新封面操作
     */
    @POST("user/acct/data/updateCover")
    suspend fun updateCover(@Body form: SaveCoverForm): Root<UserProcessBean>

    /**
     * 更新头像
     */
    @POST("user/acct/data/updateHeadPic")
    suspend fun updateHeadPic(@Body form: UserUpdateHeadForm): Root<UserHeadChangeBean>


    /**
     * 初始化身材
     */
    @POST("user/acct/data/initFigure")
    suspend fun initFigure(@Body form: EmptyForm = EmptyForm()): Root<FigureBean>
}