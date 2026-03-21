import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { GeneratorService } from '../core/services/generator';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-generator',
  templateUrl: './generator.html',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule]
})
export class GeneratorComponent {

  passwords: string[] = [];
  copied = false;

  options = {
    length: 16,
    uppercase: true,
    lowercase: true,
    numbers: true,
    symbols: true,
    excludeSimilar: true,
    count: 5
  };

  constructor(private generatorService: GeneratorService, private router: Router) {}

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    this.router.navigate(['/login']);
  }

  generate() {
    this.generatorService.generate(this.options).subscribe({
      next: (res) => {
        this.passwords = res.passwords || [];
        this.copied = false;
      },
      error: () => {
        Swal.fire('Error', 'Failed to generate passwords. Please try again.', 'error');
      }
    });
  }

  copyToClipboard(password: string) {
    navigator.clipboard.writeText(password).then(() => {
      Swal.fire({ icon: 'success', title: 'Copied!', timer: 1000, showConfirmButton: false });
    });
  }

  usePassword(password: string) {
    localStorage.setItem('generatedPassword', password);
    Swal.fire({ icon: 'info', title: 'Saved', text: 'Password saved. Go to Vault to use it.', timer: 1500, showConfirmButton: false });
  }

  getStrengthLabel(password: string): string {
    let score = 0;
    if (password.length >= 8) score++;
    if (password.length >= 12) score++;
    if (/[A-Z]/.test(password)) score++;
    if (/[a-z]/.test(password)) score++;
    if (/[0-9]/.test(password)) score++;
    if (/[!@#$%^&*()]/.test(password)) score++;
    if (score <= 2) return 'Weak';
    if (score <= 4) return 'Medium';
    if (score === 5) return 'Strong';
    return 'Very Strong';
  }

  getStrengthClass(password: string): string {
    const label = this.getStrengthLabel(password);
    const map: Record<string, string> = {
      'Weak': 'text-danger',
      'Medium': 'text-warning',
      'Strong': 'text-primary',
      'Very Strong': 'text-success'
    };
    return map[label] || '';
  }
}
