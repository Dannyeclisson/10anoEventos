import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { UsuarioPerfil } from '../../models/usuario.model';

@Component({
  selector: 'app-profile-info-card',
  standalone: true,
  imports: [MatButtonModule, MatCardModule, MatDividerModule, MatIconModule],
  templateUrl: './profile-info-card.html',
  styleUrl: './profile-info-card.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProfileInfoCardComponent {
  @Input({ required: true }) usuario!: UsuarioPerfil;
}
