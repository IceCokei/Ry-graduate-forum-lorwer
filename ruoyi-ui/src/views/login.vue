<template>
  <div class="login">
    <!-- 账号密码登录表单 -->
    <el-form ref="loginForm" :model="loginForm" :rules="loginRules" class="login-form" v-show="loginType === 'account'">
      <h3 class="title">若依后台管理系统</h3>
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
      <el-checkbox v-model="loginForm.rememberMe" style="margin:0px 0px 25px 0px;">记住密码</el-checkbox>
      <el-button type="text" style="float:right; padding: 3px 0; margin-top: -25px;" @click="switchToMobileLogin">
        手机号验证码登录
      </el-button>
      <el-form-item style="width:100%;">
        <el-button :loading="loading" size="medium" type="primary" style="width:100%;"
          @click.native.prevent="handleLogin">
          <span v-if="!loading">登 录</span>
          <span v-else>登 录 中...</span>
        </el-button>
        <div style="float: right;" v-if="register">
          <router-link class="link-type" :to="'/register'">立即注册3131</router-link>
        </div>
      </el-form-item>
    </el-form>

    <!-- 手机号验证码登录表单 -->
    <el-form ref="mobileLoginForm" :model="mobileForm" :rules="mobileRules" class="login-form"
      v-show="loginType === 'mobile'">
      <el-form-item prop="mobile">
        <el-input v-model="mobileForm.mobile" type="text" placeholder="手机号" prefix-icon="el-icon-mobile-phone">
        </el-input>
      </el-form-item>
      <el-form-item prop="code">
        <el-input v-model="mobileForm.code" placeholder="验证码" style="width: 63%" prefix-icon="el-icon-message">
        </el-input>
        <el-button :disabled="codeBtnDisabled" class="getCodeBtn" @click="getMobileCode"
          style="width: 35%; float: right; margin-top: 1px;">
          {{ codeText }}
        </el-button>
      </el-form-item>
      <el-button :loading="loading" type="primary" style="width:100%; margin-bottom:30px;" @click="mobileLogin">
        登录
      </el-button>
    </el-form>

    <!--  底部  -->
    <div class="el-login-footer">
      <span>Copyright © 2018-2021 ruoyi.vip All Rights Reserved.</span>
    </div>
  </div>
</template>

<script>
import { getCodeImg } from "@/api/login";
import Cookies from "js-cookie";
import { encrypt, decrypt } from '@/utils/jsencrypt'

export default {
  name: "Login",
  data() {
    return {
      codeUrl: "",
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
      register: false,
      redirect: undefined,
      // 添加手机号登录相关数据
      loginType: 'account', // 'account'或'mobile'
      mobileForm: {
        mobile: '',
        code: ''
      },
      mobileRules: {
        mobile: [
          { required: true, message: '请输入手机号', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
        ],
        code: [
          { required: true, message: '请输入验证码', trigger: 'blur' },
          { min: 4, max: 6, message: '验证码长度在4到6个字符', trigger: 'blur' }
        ]
      },
      codeBtnDisabled: false,
      codeText: '获取验证码',
      codeTimer: null
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
            this.$router.push({ path: this.redirect || "/" }).catch(() => { });
          }).catch(() => {
            this.loading = false;
            if (this.captchaOnOff) {
              this.getCode();
            }
          });
        }
      });
    },
    // 切换到手机号登录
    switchToMobileLogin() {
      this.loginType = this.loginType === 'account' ? 'mobile' : 'account';
      // 清空表单
      if (this.loginType === 'mobile') {
        this.$refs.loginForm.resetFields();
      } else {
        this.$refs.mobileLoginForm && this.$refs.mobileLoginForm.resetFields();
      }
    },
    // 获取手机验证码
    getMobileCode() {
      if (!this.mobileForm.mobile) {
        this.$message.error('请先输入手机号');
        return;
      }

      if (!/^1[3-9]\d{9}$/.test(this.mobileForm.mobile)) {
        this.$message.error('请输入正确的手机号');
        return;
      }

      this.codeBtnDisabled = true;
      let countdown = 60;
      this.codeText = countdown + 's';

      this.codeTimer = setInterval(() => {
        countdown--;
        this.codeText = countdown + 's';
        if (countdown <= 0) {
          clearInterval(this.codeTimer);
          this.codeText = '获取验证码';
          this.codeBtnDisabled = false;
        }
      }, 1000);

      // 调用发送验证码接口
      // this.$http.post('/captcha/mobile', { mobile: this.mobileForm.mobile }).then(res => {
      //   this.$message.success('验证码已发送');
      // }).catch(err => {
      //   this.$message.error('验证码发送失败');
      //   clearInterval(this.codeTimer);
      //   this.codeText = '获取验证码';
      //   this.codeBtnDisabled = false;
      // });
    },
    // 手机号登录
    mobileLogin() {
      this.$refs.mobileLoginForm.validate(valid => {
        if (valid) {
          this.loading = true;
          // 调用手机号登录接口
          // this.$store.dispatch('Login', this.mobileForm).then(() => {
          //   this.loading = false;
          //   this.$router.push({ path: this.redirect || '/' });
          // }).catch(() => {
          //   this.loading = false;
          // });
        }
      });
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
  background-image: url("../assets/images/login-background.jpg");
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
</style>
