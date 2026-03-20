import { Injectable } from '@angular/core';

import { environment } from '../../../environments/environment';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private baseUrl = environment.apiUrl;
private forgotBaseUrl = 'http://localhost:8080/api';

constructor(private http: HttpClient) {}

  register(data: any) {
    return this.http.post(`${this.baseUrl}/register`, data);
  }
getQuestions() {
  return this.http.get<any[]>(
    'http://localhost:8080/api/auth/security-questions'
  );
}
  login(data: any) {

  return this.http.post<any>(
    'http://localhost:8080/api/auth/login',
    data
  );

}


  saveToken(token: string) {
    localStorage.setItem('token', token);
  }

  getToken() {
    return localStorage.getItem('token');
  }

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('twoFactorEnabled');
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }
verifyMasterPassword(password: string) {
  return this.http.post(
    `${this.baseUrl}/api/auth/verify-master-password`,
    { masterPassword: password }
  );
}
getProfile() {
  return this.http.get('http://localhost:8080/api/user/profile');
}

updateProfile(data: any) {
  return this.http.put(`${this.baseUrl}/api/users/profile`, data);
}
changePassword(data: any) {
  return this.http.post(`${this.baseUrl}/api/auth/change-password`, data);
}

getSecurityQuestions() {
  return this.http.get(`${this.baseUrl}/api/auth/security-questions`);
}

updateSecurityQuestions(data: any) {
  return this.http.put(`${this.baseUrl}/api/auth/security-questions`, data);
}
requestRecovery(email: string) {
  return this.http.post(`${this.baseUrl}/api/auth/forgot-password`, { email });
}
generateOtp(username: string) {
  return this.http.post(
    `${this.forgotBaseUrl}/otp/generate`,
    { username },
    { responseType: 'text' as 'json' }
  );
}
verifyOtp(data: any) {
  return this.http.post(
    `${this.forgotBaseUrl}/otp/verify`,
    data,
    { responseType: 'text' as 'json' }
  );
}
getRecoveryQuestions(username: string) {
  return this.http.get(
    `${this.forgotBaseUrl}/forgot/questions/${username}`
  );
}

verifySecurityAnswers(data: any) {
  return this.http.post(
    `${this.forgotBaseUrl}/forgot/verify`,
    data,
    { responseType: 'text' as 'json' }
  );
}

resetPassword(data: any) {
  return this.http.post(
    `${this.forgotBaseUrl}/forgot/reset`,
    data,
    { responseType: 'text' as 'json' }
  );
}
checkUser(username: string) {
  return this.http.get(`http://localhost:8080/api/forgot/user-exists/${username}`)
}
enable2FA(enabled: boolean) {
  return this.http.post(
    `${this.baseUrl}/api/profile/2fa`,
    { enabled },
    { responseType: 'text' as 'json' }
  );
}

verify2FA(data: any) {
  return this.http.post(
    'http://localhost:8080/api/auth/verify-otp',
    data
  );
}
disable2FA() {
  return this.http.post(`${this.baseUrl}/api/auth/2fa`, {});
}
}
