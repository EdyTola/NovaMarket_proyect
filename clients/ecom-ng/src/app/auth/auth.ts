import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../core/auth/auth.service';

@Component({
  selector: 'app-auth',
  imports: [CommonModule, FormsModule],
  templateUrl: './auth.html',
  styleUrl: './auth.scss',
})
export class Auth {
  username = '';
  password = '';
  loading = signal(false);
  error = signal('');

  constructor(
    private auth: AuthService,
    private router: Router,
    private route: ActivatedRoute,
  ) {}

  login() {
    this.error.set('');
    this.loading.set(true);

    this.auth.login({
      username: this.username.trim(),
      password: this.password,
    }).subscribe({
      next: () => {
        const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl') ?? '/pos';
        this.router.navigateByUrl(returnUrl);
      },
      error: (err: HttpErrorResponse) => {
        if (err.status === 0) {
          this.error.set(
            'No hay conexión con el gateway (http://localhost:18080). Levante Eureka, config-server, gateway y services/ms-auth.',
          );
        } else if (err.status === 401 || err.status === 403) {
          this.error.set('Usuario o contraseña incorrectos. Pruebe cajero / cajero123');
        } else if (err.status === 404 || err.status === 503) {
          this.error.set(
            'El gateway no encuentra ms-auth. Reinicie services/ms-auth y verifique en Eureka MS-AUTH.',
          );
        } else {
          this.error.set(`Error al iniciar sesión (${err.status})`);
        }
        this.loading.set(false);
      },
      complete: () => this.loading.set(false),
    });
  }
}
