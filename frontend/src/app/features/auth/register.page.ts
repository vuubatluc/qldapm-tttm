import { Component, inject, signal } from '@angular/core';
import {
  AbstractControl,
  NonNullableFormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { toErrorMessage } from '../../core/http/api-error';
import { PASSWORD_PATTERN, PHONE_PATTERN } from '../../shared/validation';

/** Khách thuê tự đăng ký; tài khoản ở trạng thái Chờ duyệt cho đến khi BQL phê duyệt. */
@Component({
  selector: 'app-register-page',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register.page.html',
  styleUrl: './auth-page.scss',
})
export class RegisterPage {
  private readonly auth = inject(AuthService);

  protected readonly submitting = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly done = signal(false);

  protected readonly form = inject(NonNullableFormBuilder).group(
    {
      fullName: ['', [Validators.required, Validators.maxLength(150)]],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', Validators.pattern(PHONE_PATTERN)],
      password: ['', [Validators.required, Validators.pattern(PASSWORD_PATTERN)]],
      confirmPassword: ['', Validators.required],
    },
    { validators: passwordsMatch },
  );

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const { fullName, email, phone, password } = this.form.getRawValue();
    this.submitting.set(true);
    this.error.set(null);
    this.auth.register({ fullName, email, phone: phone || null, password }).subscribe({
      next: () => this.done.set(true),
      error: (err: unknown) => {
        this.error.set(toErrorMessage(err));
        this.submitting.set(false);
      },
    });
  }
}

function passwordsMatch(group: AbstractControl): ValidationErrors | null {
  const pw = group.get('password')?.value as string;
  const confirm = group.get('confirmPassword')?.value as string;
  return pw && confirm && pw !== confirm ? { passwordMismatch: true } : null;
}
