<template>
  <div class="login">
    <!-- 账号密码登录表单 -->
    <el-form ref="loginForm" :model="loginForm" :rules="loginRules" class="login-form" v-show="loginType === 'account'">
      <h3 class="title">登录</h3>
      <el-form-item prop="username">
        <el-input v-model="loginForm.username" type="text" auto-complete="off" placeholder="账号">
          <svg-icon slot="prefix" icon-class="user" class="el-input__icon input-icon" />
        </el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input v-model="loginForm.password" type="password" auto-complete="off" placeholder="密码"
          @keyup.enter.native="handleLogin">
          <svg-icon slot="prefix" icon-class="password" class="el-input__icon input-icon" />
        </el-input>
      </el-form-item>
      <el-form-item prop="code" v-if="captchaOnOff">
        <el-input v-model="loginForm.code" auto-complete="off" placeholder="验证码" style="width: 63%"
          @keyup.enter.native="handleLogin">
          <svg-icon slot="prefix" icon-class="validCode" class="el-input__icon input-icon" />
        </el-input>
        <div class="login-code">
          <img :src="codeUrl" @click="getCode" class="login-code-img" />
        </div>
      </el-form-item>
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 25px;">
        <el-checkbox v-model="loginForm.rememberMe">记住密码</el-checkbox>
        <button @click.prevent="switchToMobileLogin" class="verify-code-btn">
          验证码登录
        </button>
      </div>
      <el-form-item style="width:100%;">
        <el-button :loading="loading" size="medium" type="primary" style="width:100%;"
          @click.native.prevent="handleLogin">
          <span v-if="!loading">登 录</span>
          <span v-else>登 录 中...</span>
        </el-button>
        <div style="float: right;" v-if="register">
          <router-link class="link-type" :to="'/cmsRegister'">立即注册</router-link>
        </div>
      </el-form-item>
    </el-form>

    <!-- 手机号验证码登录表单 -->
    <el-form ref="mobileLoginForm" :model="mobileForm" :rules="mobileRules" class="login-form"
      v-show="loginType === 'mobile'">
      <h3 class="title">绘梦考研论坛</h3>
      <el-form-item prop="mobile">
        <el-input v-model="mobileForm.mobile" type="text" auto-complete="off" placeholder="手机号" @blur="validateMobile">
          <svg-icon slot="prefix" icon-class="user" class="el-input__icon input-icon" />
        </el-input>
      </el-form-item>
      <!-- 图片验证码，只有手机号验证通过后才显示 -->
      <el-form-item prop="imgCode" v-if="captchaOnOff && mobileValidated">
        <el-input v-model="mobileForm.imgCode" auto-complete="off" placeholder="图片验证码" style="width: 63%"
          @blur="validateImgCode" @input="onImgCodeInput">
          <svg-icon slot="prefix" icon-class="validCode" class="el-input__icon input-icon" />
        </el-input>
        <div class="login-code">
          <img :src="mobileCodeUrl" @click="getMobileImgCode" class="login-code-img" />
        </div>
      </el-form-item>
      <el-form-item prop="code">
        <el-input v-model="mobileForm.code" auto-complete="off" placeholder="短信验证码" style="width: 63%">
          <svg-icon slot="prefix" icon-class="validCode" class="el-input__icon input-icon" />
        </el-input>
        <el-button :disabled="codeBtnDisabled || !mobileValidated || !imgCodeValidated" class="getCodeBtn"
          @click="getMobileCode" style="width: 35%; float: right; margin-top: 1px;">
          {{ codeText }}
        </el-button>
      </el-form-item>
      <el-button :loading="loading" type="primary" style="width:100%; margin-bottom:30px;" @click="mobileLogin">
        登录
      </el-button>
      <el-button type="text" style="width:100%;" @click.prevent="switchToMobileLogin">
        返回账号密码登录
      </el-button>
    </el-form>

    <!--  底部  -->
    <div class="el-login-footer">
      <span>Copyright © 2018-2021 ruoyi.vip All Rights Reserved.</span>
    </div>
  </div>
</template>

<script>
import { getCodeImg, sendSmsCode, mobileLoginApi } from "@/api/login";
import Cookies from "js-cookie";
import { encrypt, decrypt } from '@/utils/jsencrypt'

export default {
  name: "cmsLogin",
  data() {
    return {
      codeUrl: "",
      mobileCodeUrl: "",
      loginForm: {
        username: "admin",
        password: "admin123",
        rememberMe: false,
        code: "",
        uuid: ""
      },
      loginRules: {
        username: [
          { required: true, trigger: "blur", message: "请输入您的账号" }
        ],
        password: [
          { required: true, trigger: "blur", message: "请输入您的密码" }
        ],
        code: [{ required: true, trigger: "change", message: "请输入验证码" }]
      },
      loading: false,
      // 验证码开关
      captchaOnOff: true,
      // 注册开关
      register: true,
      redirect: undefined,
      // 添加手机号登录相关数据
      loginType: 'account', // 'account'或'mobile'
      mobileForm: {
        mobile: '',
        imgCode: '',
        code: '',
        uuid: ''
      },
      mobileRules: {
        mobile: [
          { required: true, message: '请输入手机号', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的11位手机号', trigger: 'blur' }
        ],
        imgCode: [
          { required: true, trigger: "change", message: "请输入图片验证码" }
        ],
        code: [
          { required: true, message: '请输入验证码', trigger: 'blur' },
          { min: 4, max: 6, message: '验证码长度在4到6个字符', trigger: 'blur' }
        ]
      },
      codeBtnDisabled: false,
      codeText: '获取验证码',
      codeTimer: null,
      // 添加新的状态
      mobileValidated: false, // 手机号是否已验证通过
      imgCodeValidated: false, // 图片验证码是否已验证通过
    };
  },
  watch: {
    $route: {
      handler: function (route) {
        this.redirect = route.query && route.query.redirect;
      },
      immediate: true
    }
  },
  created() {
    this.getCode();
    this.getCookie();
    this.getMobileImgCode();
  },
  methods: {
    getCode() {
      getCodeImg().then(res => {
        this.captchaOnOff = res.captchaOnOff === undefined ? true : res.captchaOnOff;
        if (this.captchaOnOff) {
          this.codeUrl = "data:image/gif;base64," + res.img;
          this.loginForm.uuid = res.uuid;
        }
      });
    },
    getCookie() {
      const username = Cookies.get("username");
      const password = Cookies.get("password");
      const rememberMe = Cookies.get('rememberMe')
      this.loginForm = {
        username: username === undefined ? this.loginForm.username : username,
        password: password === undefined ? this.loginForm.password : decrypt(password),
        rememberMe: rememberMe === undefined ? false : Boolean(rememberMe)
      };
    },
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.loading = true;
          if (this.loginForm.rememberMe) {
            Cookies.set("username", this.loginForm.username, { expires: 30 });
            Cookies.set("password", encrypt(this.loginForm.password), { expires: 30 });
            Cookies.set('rememberMe', this.loginForm.rememberMe, { expires: 30 });
          } else {
            Cookies.remove("username");
            Cookies.remove("password");
            Cookies.remove('rememberMe');
          }
          this.$store.dispatch("Login", this.loginForm).then(() => {
            this.$router.push({ path: "/cms/main/cmsIndex" });
          }).catch(() => {
            this.loading = false;
            if (this.captchaOnOff) {
              this.getCode();
            }
          });
        }
      });
    },
    // 验证手机号
    validateMobile() {
      if (!this.mobileForm.mobile) {
        this.mobileValidated = false;
        return;
      }

      // 验证手机号格式
      if (!/^1[3-9]\d{9}$/.test(this.mobileForm.mobile)) {
        this.$message.error('请输入正确的11位手机号');
        this.mobileValidated = false;
        return;
      }

      // 手机号验证通过后自动获取图片验证码
      this.mobileValidated = true;
      this.getMobileImgCode();
    },

    // 验证图片验证码
    validateImgCode() {
      if (!this.mobileForm.imgCode && this.captchaOnOff) {
        this.imgCodeValidated = false;
        return false;
      }

      // 模拟验证成功 (实际项目中应该调用后端验证)
      // 在真实环境中，您应该发送请求到后端验证图片验证码是否正确
      this.imgCodeValidated = true;
      return true;
    },

    // 获取手机登录的图片验证码
    getMobileImgCode() {
      getCodeImg().then(res => {
        this.captchaOnOff = res.captchaOnOff === undefined ? true : res.captchaOnOff;
        if (this.captchaOnOff) {
          this.mobileCodeUrl = "data:image/gif;base64," + res.img;
          this.mobileForm.uuid = res.uuid;
          // 重置图片验证码验证状态
          this.imgCodeValidated = false;
          // 重置验证码输入框
          this.mobileForm.imgCode = '';
        }
      });
    },

    // 获取手机验证码
    getMobileCode() {
      if (!this.mobileValidated) {
        this.$message.error('请先输入正确的手机号');
        return;
      }

      // 验证图片验证码
      if (!this.validateImgCode()) {
        this.$message.error('请输入正确的图片验证码');
        return;
      }

      // 禁用按钮，开始倒计时
      this.codeBtnDisabled = true;
      let countdown = 60;
      this.codeText = countdown + 's';

      // 模拟发送验证码成功 (后端接口未实现时使用)
      this.$message.success('验证码已发送，请注意查收');

      // 开始倒计时
      this.codeTimer = setInterval(() => {
        countdown--;
        this.codeText = countdown + 's';
        if (countdown <= 0) {
          clearInterval(this.codeTimer);
          this.codeText = '获取验证码';
          this.codeBtnDisabled = false;
        }
      }, 1000);

      // 模拟生成验证码，实际应用中应由后端发送到手机上
      // 为了演示方便，将验证码设为123456并在控制台输出
      console.log('模拟验证码: 123456');
    },

    // 手机号登录
    mobileLogin() {
      this.$refs.mobileLoginForm.validate(valid => {
        if (valid) {
          // 模拟验证码校验 - 在实际应用中应由后端验证
          if (this.mobileForm.code === '123456' || this.mobileForm.code === '') {
            this.loading = true;

            // 模拟登录成功
            setTimeout(() => {
              this.$message.success('登录成功');
              this.$router.push({ path: "/cms/main/cmsIndex" });
            }, 1000);
          } else {
            this.$message.error('验证码错误');
            // 刷新图片验证码
            if (this.captchaOnOff) {
              this.getMobileImgCode();
            }
          }
        }
      });
    },

    // 切换到手机号登录
    switchToMobileLogin(event) {
      // 阻止事件冒泡和默认行为
      event && event.preventDefault();

      this.loginType = this.loginType === 'account' ? 'mobile' : 'account';

      // 清空表单
      if (this.loginType === 'mobile') {
        this.$refs.loginForm && this.$refs.loginForm.resetFields();
        // 重置手机号验证状态
        this.mobileValidated = false;
        this.imgCodeValidated = false;
      } else {
        this.$refs.mobileLoginForm && this.$refs.mobileLoginForm.resetFields();
        if (this.captchaOnOff) {
          this.getCode();
        }
      }

      // 重置加载状态
      this.loading = false;

      // 阻止事件继续传播
      return false;
    },

    // 处理图片验证码输入
    onImgCodeInput() {
      // 如果图片验证码长度达到4位，自动验证
      if (this.mobileForm.imgCode && this.mobileForm.imgCode.length >= 4) {
        this.validateImgCode();
      } else {
        this.imgCodeValidated = false;
      }
    }
  },
  beforeDestroy() {
    // 清除定时器
    if (this.codeTimer) {
      clearInterval(this.codeTimer);
    }
  }
};
</script>

<style rel="stylesheet/scss" lang="scss">
.login {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  background-image: url("../../assets/images/login-background.jpg");
  background-size: cover;
}

.title {
  margin: 0px auto 30px auto;
  text-align: center;
  color: #707070;
}

.login-form {
  border-radius: 6px;
  background: #ffffff;
  width: 400px;
  padding: 25px 25px 5px 25px;

  .el-input {
    height: 38px;

    input {
      height: 38px;
    }
  }

  .input-icon {
    height: 39px;
    width: 14px;
    margin-left: 2px;
  }
}

.login-tip {
  font-size: 13px;
  text-align: center;
  color: #bfbfbf;
}

.login-code {
  width: 33%;
  height: 38px;
  float: right;

  img {
    cursor: pointer;
    vertical-align: middle;
  }
}

.el-login-footer {
  height: 40px;
  line-height: 40px;
  position: fixed;
  bottom: 0;
  width: 100%;
  text-align: center;
  color: #fff;
  font-family: Arial;
  font-size: 12px;
  letter-spacing: 1px;
}

.login-code-img {
  height: 38px;
}

.getCodeBtn {
  border-radius: 4px;
}

.verify-code-btn {
  background-color: #ecf5ff;
  color: #409EFF;
  border: 1px solid #d9ecff;
  border-radius: 4px;
  padding: 8px 15px;
  font-size: 12px;
  cursor: pointer;
  outline: none;
  margin-left: auto;
  line-height: normal;
  height: 32px;
}

.verify-code-btn:hover {
  background-color: #d9ecff;
}
</style>
