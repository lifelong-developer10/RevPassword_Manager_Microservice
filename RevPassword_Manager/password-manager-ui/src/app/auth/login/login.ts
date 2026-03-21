import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { RouterModule } from '@angular/router';
import { Component, ViewEncapsulation, NgZone, ChangeDetectorRef } from '@angular/core';
import { NotificationService } from '../../core/services/notification.service';
import { ProfileService } from '../../core/services/profile.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-login',
  standalone: true,
  templateUrl: './login.html',
  styleUrls: ['./login.css'],
  encapsulation: ViewEncapsulation.None,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterModule]
})
export class LoginComponent {

  show2FAScreen = false;
  twoFACode = '';
  showPassword = false;
  form: any;

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private router: Router,
    private notificationService: NotificationService,
    private profileService: ProfileService,
    private zone: NgZone,
    private cd: ChangeDetectorRef
  ) {
    this.form = this.fb.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  ngOnInit() {
    const savedPassword = localStorage.getItem('generatedPassword');
    if (savedPassword && savedPassword.length >= 8) {
      this.form.patchValue({ password: savedPassword });
    }
  }

  togglePassword() {
    this.showPassword = !this.showPassword;
  }

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('twoFactorEnabled');
    this.router.navigate(['/landing']);
  }

  verifyLogin2FA() {
    const data = {
      username: this.form.value.username,
      otp: this.twoFACode
    };

    this.auth.verify2FA(data).subscribe({
      next: (res: any) => {
        console.log("OTP RESPONSE:", res);

        if (res.message === "INVALID_OTP") {
          Swal.fire('Invalid OTP', 'Please try again', 'error');
          return;
        }

        localStorage.setItem("token", res.token);
        Swal.fire('Login Successful', '', 'success').then(() => {
          this.router.navigate(['/dashboard']);
        });
      },
      error: (err) => {
        console.error(err);
        Swal.fire('OTP Verification Failed', '', 'error');
      }
    });
  }

  login() {
    if (this.form.invalid) return;
    if (this.show2FAScreen) return;

    this.auth.login(this.form.value).subscribe({
      next: (res: any) => {
        console.log("LOGIN RESPONSE:", res);

        if (res.token === "OTP_REQUIRED") {
          // Fix: use zone.run + detectChanges so *ngIf re-evaluates immediately
          this.zone.run(() => {
            this.show2FAScreen = true;
            this.cd.detectChanges();
          });
          Swal.fire('OTP Sent', 'Check your email for the OTP', 'info');
          return;
        }

        const token = res.token;
        const username = this.form.value.username;
        localStorage.setItem("token", token);
        localStorage.setItem("username", username);

        this.notificationService.getNotifications(username).subscribe((data: any[]) => {
          const unreadCount = data.filter((n: any) => !n.readStatus).length;
          this.notificationService.setNotificationCount(unreadCount);
        });

        this.profileService.getProfile().subscribe((profile: any) => {
          const twoFA = profile.twoFactorEnabled ?? false;
          localStorage.setItem("twoFactorEnabled", String(twoFA));
        });

        Swal.fire('Login Successful', '', 'success').then(() => {
          this.router.navigate(['/dashboard']);
        });
      },
      error: (err) => {
        console.error(err);
        const msg = err?.error?.error || 'Invalid credentials';
        Swal.fire('Login Failed', msg, 'error');
      }
    });
  }
}
