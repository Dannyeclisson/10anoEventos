import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-benefit-card',
  standalone: true,
  imports: [MatCardModule, MatIconModule],
  templateUrl: './benefit-card.html',
  styleUrl: './benefit-card.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class BenefitCardComponent {
  @Input({ required: true }) titulo = '';
  @Input({ required: true }) descricao = '';
  @Input({ required: true }) icone = '';
}
