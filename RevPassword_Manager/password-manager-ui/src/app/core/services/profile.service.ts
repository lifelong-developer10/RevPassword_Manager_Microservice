import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class ProfileService {

  private API = 'http://localhost:8080/api/profile';

  constructor(private http: HttpClient) {}

  getProfile() {
    return this.http.get(this.API);
  }

  updateProfile(data: any) {
    return this.http.put(this.API, data);
  }

  getQuestions() {
    return this.http.get(this.API + '/security-questions');
  }

  updateQuestions(data: any) {
    return this.http.put(this.API + '/security-questions', data);
  }

  changePassword(data: any) {
    return this.http.post(this.API + '/change-password', data);
  }

 update2FA(enabled: boolean) {

  return this.http.post(
    'http://localhost:8080/api/profile/2fa',
    { enabled },
    { responseType: 'text' as 'json' }
  );

}

  verifyMasterPassword(masterPassword: string) {
    return this.http.post(
      'http://localhost:8080/api/profile/verify-master-password',
      { masterPassword }
    );
  }

}
