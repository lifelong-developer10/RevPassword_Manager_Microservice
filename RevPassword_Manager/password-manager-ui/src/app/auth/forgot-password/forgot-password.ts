import { Component } from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import Swal from 'sweetalert2';
import { AuthService } from '../../core/services/auth.service';
import { NgZone } from '@angular/core';
import { ChangeDetectorRef } from '@angular/core';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, FormsModule],

  templateUrl: './forgot-password.html',
  styleUrls: ['./forgot-password.css']
})
export class ForgotPasswordComponent {

  // VIEW STATE
  view: 'username' | 'method' | 'verifyOtp' | 'verifyQuestions' | 'reset' = 'username';

  progress = 25;

  username = '';
  otp = '';
  newPassword = '';

  method: 'otp' | 'questions' | '' = '';

  questions: any[] = [];

  loading = false;

  constructor(
    private auth: AuthService,
    private router: Router,
    private zone: NgZone,

       private cd: ChangeDetectorRef
  ) {  console.log("FORGOT COMPONENT CREATED");
}

  setView(v: 'username' | 'method' | 'verifyOtp' | 'verifyQuestions' | 'reset') {

    this.view = v;

    switch (v) {
      case 'username':
        this.progress = 25;
        break;

      case 'method':
        this.progress = 50;
        break;

      case 'verifyOtp':
      case 'verifyQuestions':
        this.progress = 75;
        break;

      case 'reset':
        this.progress = 100;
        break;
    }

  }

 continue() {

   console.log("CLICKED CONTINUE");

   if (!this.username) return;

   this.loading = true;

   this.auth.checkUser(this.username).subscribe({

    next: (exists: any) => {

      console.log("API RESPONSE:", exists);

      this.loading = false;

      if (!exists) return;

      this.setView('method');

      this.cd.detectChanges();   // ⭐ IMPORTANT

      console.log("VIEW NOW:", this.view);
    },

     error: (err) => {

       console.log("API ERROR:", err);
       this.loading = false;

       Swal.fire('Error', 'Server error', 'error');
     }

   });

 }
  // OTP SELECT
  selectOtp() {

    this.method = 'otp';
    this.loading = true;

    this.auth.generateOtp(this.username).subscribe({

      next: () => {
        this.loading = false;
        this.zone.run(() => {
          this.setView('verifyOtp');
          this.cd.detectChanges();
        });
        Swal.fire('OTP Sent', 'Check your email', 'success');
      },

      error: () => {
        this.loading = false;
        Swal.fire('Error', 'OTP failed', 'error');
      }

    });
  }

  // QUESTIONS SELECT
  selectQuestions() {

    this.method = 'questions';
    this.loading = true;

    this.auth.getRecoveryQuestions(this.username).subscribe({

      next: (res: any) => {
        this.loading = false;
        this.questions = res;
        this.zone.run(() => {
          this.setView('verifyQuestions');
          this.cd.detectChanges();
        });
      },

      error: () => {
        this.loading = false;
        Swal.fire('Error', 'Cannot load questions', 'error');
      }

    });
  }

  // VERIFY OTP
  verifyOtp() {

    this.loading = true;

    this.auth.verifyOtp({
      username: this.username,
      code: this.otp
    }).subscribe({

      next: (res: any) => {
        this.loading = false;

        // response is plain text: "OTP Verified" or "Invalid OTP"
        if (res === 'OTP Verified') {
          Swal.fire('Verified', 'OTP correct', 'success').then(() => {
            this.zone.run(() => {
              this.setView('reset');
              this.cd.detectChanges();
            });
          });
        } else {
          Swal.fire('Error', 'Invalid OTP', 'error');
        }
      },

      error: () => {
        this.loading = false;
        Swal.fire('Error', 'Invalid OTP', 'error');
      }

    });
  }

  verifyQuestions() {

    const answers: any = {};

    this.questions.forEach(q => {
      answers[q.questionId] = q.answer;
    });

    this.loading = true;

    this.auth.verifySecurityAnswers({
      username: this.username,
      answers: answers
    }).subscribe({

      next: (res: any) => {
        this.loading = false;

        if (res === 'VERIFIED') {
          Swal.fire('Correct!', 'Answers verified', 'success').then(() => {
            this.zone.run(() => {
              this.setView('reset');
              this.cd.detectChanges();
            });
          });
        } else {
          Swal.fire('Incorrect', 'One or more answers are wrong', 'error');
        }
      },

      error: () => {
        this.loading = false;
        Swal.fire('Verification failed', '', 'error');
      }

    });

  }

  // RESET PASSWORD
  resetPassword() {

    if (!this.newPassword) {
      Swal.fire('Error', 'Enter new password', 'error');
      return;
    }

    this.loading = true;

    this.auth.resetPassword({
      username: this.username,
      newPassword: this.newPassword
    }).subscribe({

      next: () => {

        this.loading = false;

        Swal.fire('Success', 'Password updated', 'success');

        this.router.navigate(['/login']);

      },

      error: () => {
        this.loading = false;
        Swal.fire('Error', 'Reset failed', 'error');
      }

    });
  }

}
