import { ChangeDetectionStrategy, Component } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { RouterLink } from '@angular/router';
import { BenefitCardComponent } from '../../components/benefit-card/benefit-card';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    BenefitCardComponent,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatSnackBarModule,
    RouterLink
  ],
  templateUrl: './home.html',
  styleUrl: './home.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class HomeComponent {
  readonly benefits = [
    {
      titulo: 'Organização de eventos',
      descricao:
        'Centralize informações e transforme boas ideias em encontros bem organizados.',
      icone: 'event_note'
    },
    {
      titulo: 'Conexão com parceiros',
      descricao:
        'Encontre apoio, fornecedores e iniciativas alinhadas ao propósito do evento.',
      icone: 'handshake'
    },
    {
      titulo: 'Gerenciamento de participantes',
      descricao:
        'Acompanhe confirmações e mantenha a comunidade informada em um só lugar.',
      icone: 'groups'
    },
    {
      titulo: 'Participação comunitária',
      descricao:
        'Descubra oportunidades para colaborar e fortalecer conexões locais.',
      icone: 'diversity_3'
    }
  ];

  constructor(private readonly snackBar: MatSnackBar) {}

  showComingSoon(): void {
    this.snackBar.open(
      'A criação de eventos estará disponível em uma próxima etapa.',
      'Entendi',
      { duration: 4000 }
    );
  }
}
