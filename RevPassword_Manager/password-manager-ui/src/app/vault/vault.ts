import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { VaultService } from '../core/services/vault.service';
import { Router } from '@angular/router';
import { ActivatedRoute } from '@angular/router';
import Swal from 'sweetalert2';
import { ProfileService } from '../core/services/profile.service';
import { ChangeDetectorRef } from '@angular/core';

@Component({
  selector: 'app-vault',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './vault.html',
  styleUrls: ['./vault.css']
})
export class VaultComponent implements OnInit {

  vaults: any[] = [];
  keyword = '';

filteredVaults: any[] = [];
isFavoriteFilter = false;
searchText = '';


showForm = false;
editMode = false;

form: any = {
  id: null,
  accountName: '',
  website: '',
  username: '',
  password: '',
  category: '',
  notes: '',
  favorite: false
};

constructor(
  private vaultService: VaultService,
  private route: ActivatedRoute,
  private cd: ChangeDetectorRef,
    private profileService: ProfileService,

) {}
ngOnInit() {
  this.loadVaults();
}

loadVaults() {

  this.vaultService.getAll()
    .subscribe({

      next: (res: any[]) => {

        this.vaults = res || [];
        this.filteredVaults = [...this.vaults];
        this.cd.detectChanges();

      },

      error: err => console.error(err)

    });

}
searchVault() {

  if (!this.searchText) {
    this.filteredVaults = [...this.vaults];
    return;
  }

  const text = this.searchText.toLowerCase();

  this.filteredVaults = this.vaults.filter(v =>

    v.accountName?.toLowerCase().includes(text) ||
    v.website?.toLowerCase().includes(text) ||
    v.category?.toLowerCase().includes(text) ||
(v.favorite && (text.includes('favorite') || text.includes('favourite')))
  );

}
trackById(index: number, item: any) {
  return item.id;
}
 openAdd() {
   this.editMode = false;
   this.showForm = true;
 }

openEdit(v: any) {

  Swal.fire({
    title: 'Enter Master Password',
    input: 'password',
    inputPlaceholder: 'Master Password',
    showCancelButton: true
  }).then((result) => {

    if (!result.value) return;

    this.profileService.verifyMasterPassword(result.value).subscribe({

      next: () => {

        console.log("EDIT DATA:", v);

        this.editMode = true;
        this.showForm = true;

        this.form = {
          id: v.id,
          accountName: v.accountName,
          website: v.website,
          username: v.username,
          password: v.password,
          category: v.category,
          notes: v.notes,
          favorite: v.favorite
        };

        this.cd.detectChanges();

      },

      error: () => {
        Swal.fire("Error", "Incorrect Master Password", "error");
      }

    });

  });

}
  // Generator Options
  genOptions = {
    length: 16,
    uppercase: true,
    lowercase: true,
    numbers: true,
    symbols: true,
    excludeSimilar: true
  };

  strengthScore = 0;
  strengthLabel = '';

  save() {
    const f = this.form;
    const isInvalid = !f.accountName?.trim() || !f.website?.trim() || !f.username?.trim() || !f.password?.trim() || !f.category?.trim();

    if (isInvalid) {
      Swal.fire({
        icon: 'warning',
        title: 'Invalid Input',
        text: 'Fields cannot be empty. Please fill all required details.'
      });
      return;
    }

    if (this.editMode && this.form.id) {
      console.log('Sending update for ID:', this.form.id, this.form);
      this.vaultService.update(this.form.id, this.form).subscribe({
        next: () => {
          Swal.fire({ icon: 'success', title: 'Updated', text: 'Account updated successfully' });
          this.loadVaults();
          this.showForm = false;
          this.editMode = false;
          this.resetForm();
          this.cd.detectChanges();
        },
        error: err => {
          console.error('Update Error Object:', err);
          const msg = err.error?.message || err.message || 'Update failed. Please check inputs.';
          Swal.fire({ icon: 'error', title: 'Error', text: msg });
        }
      });
    } else {
      console.log('Sending create for:', this.form);
      this.vaultService.create(this.form).subscribe({
        next: () => {
          Swal.fire({ icon: 'success', title: 'Created', text: 'Account created successfully' });
          this.loadVaults();
          this.showForm = false;
          this.resetForm();
          this.cd.detectChanges();
        },
        error: err => {
          console.error('Create Error Object:', err);
          const msg = err.error?.message || err.message || 'Creation failed. Please check inputs.';
          Swal.fire({ icon: 'error', title: 'Error', text: msg });
        }
      });
    }
  }

  delete(id: number) {
    Swal.fire({
      title: 'Are you sure?',
      text: "You won't be able to revert this!",
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#3085d6',
      cancelButtonColor: '#d33',
      confirmButtonText: 'Yes, delete it!'
    }).then((result) => {
      if (result.isConfirmed) {
        this.vaultService.delete(id).subscribe({
          next: () => {
            Swal.fire('Deleted!', 'Account has been deleted.', 'success');
            this.loadVaults();
            this.cd.detectChanges();
          },
          error: () => {
             Swal.fire('Error!', 'Failed to delete account.', 'error');
          }
        });
      }
    });
  }

  generatePassword() {
    const upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    const lower = "abcdefghijklmnopqrstuvwxyz";
    const num = "0123456789";
    const sym = "!@#$%^&*()_+{}[]<>?";
    const similar = "0O1lI";

    let pool = "";
    if (this.genOptions.uppercase) pool += upper;
    if (this.genOptions.lowercase) pool += lower;
    if (this.genOptions.numbers) pool += num;
    if (this.genOptions.symbols) pool += sym;

    let chars = pool;
    if (this.genOptions.excludeSimilar) {
      for (const c of similar) {
        chars = chars.split(c).join('');
      }
    }

    if (!chars) {
      Swal.fire('Error', 'Please select at least one character set', 'error');
      return;
    }

    let password = "";
    for (let i = 0; i < this.genOptions.length; i++) {
      password += chars.charAt(Math.floor(Math.random() * chars.length));
    }

    this.form.password = password;
    this.updateStrength(password);

    Swal.fire({
      icon: 'success',
      title: 'Generated!',
      text: 'Strong password generated based on your settings.',
      timer: 1500,
      showConfirmButton: false
    });
  }

  updateStrength(p: string) {
    let score = 0;
    if (!p) {
      this.strengthScore = 0;
      this.strengthLabel = '';
      return;
    }
    if (p.length >= 8) score++;
    if (p.length >= 12) score++;
    if (/[A-Z]/.test(p)) score++;
    if (/[0-9]/.test(p)) score++;
    if (/[!@#$%^&*()_+]/.test(p)) score++;

    this.strengthScore = score;
    const labels = ['Very Weak', 'Weak', 'Medium', 'Strong', 'Very Strong', 'Excellent'];
    this.strengthLabel = labels[score] || 'Unknown';
  }

  copyToClipboard() {
    if (!this.form.password) return;
    navigator.clipboard.writeText(this.form.password);
    Swal.fire({
      icon: 'info',
      title: 'Copied',
      text: 'Password copied to clipboard',
      timer: 1000,
      showConfirmButton: false
    });
  }

  exportAccounts() {
    this.vaultService.exportCsv().subscribe({
      next: (blob: any) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'vault_export_encrypted.csv';
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
        Swal.fire('Exported', 'Your encrypted vault has been exported to CSV', 'success');
      },
      error: () => {
        Swal.fire('Error', 'Failed to export vault', 'error');
      }
    });
  }

importAccounts(event: any) {

  const file = event.target.files[0];
  const formData = new FormData();
  formData.append("file", file);

  this.vaultService.importCsv(formData).subscribe({
    next: () => {
      Swal.fire('Imported', 'Vault imported successfully', 'success');

      this.loadVaults(); // ⭐ important
    },
    error: () => {
      Swal.fire('Error', 'Import failed', 'error');
    }
  });

}



  search() {

    if (!this.keyword) {
      this.loadVaults();
      return;
    }

    this.vaultService.search(this.keyword)
      .subscribe(res => {
        this.vaults = res || [];
        this.filteredVaults = [...this.vaults];
        this.cd.detectChanges();
      });

  }

  loadFavorites() {
    this.vaultService.favorites()
      .subscribe(res => {
        this.vaults = res || [];
        this.filteredVaults = [...this.vaults];
        this.cd.detectChanges();
      });
  }

toggleFavorites() {

  this.isFavoriteFilter = !this.isFavoriteFilter;

  if (this.isFavoriteFilter) {

    this.vaultService.favorites()
      .subscribe((res: any[]) => {

        this.filteredVaults = res || [];
  this.cd.detectChanges();   // ⭐ ADD

      });

  } else {

    this.loadVaults();   // show all again

  }

}
  resetForm() {
    this.form = {
      id: null,
      accountName: '',
      website: '',
      username: '',
      password: '',
      category: '',
      notes: '',
      favorite: false
    };
  }
togglePassword(v: any) {

  if (!v.show) {

    const master = prompt("Enter Master Password");

    if (!master) return;

    this.profileService.verifyMasterPassword(master).subscribe({

      next: () => {

        v.show = true;

        this.cd.detectChanges();

      },

      error: () => {

Swal.fire({
  icon: 'error',
  title: 'Error',
  text: 'Incorrect Password'
});
      }

    });

  } else {

    v.show = false;
    this.cd.detectChanges();

  }

}

}
