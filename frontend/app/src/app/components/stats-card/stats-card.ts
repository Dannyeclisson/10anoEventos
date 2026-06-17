import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-stats-card',
  standalone: true,
  imports: [MatCardModule, MatIconModule],
  templateUrl: './stats-card.html',
  styleUrl: './stats-card.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class StatsCardComponent {
  @Input({ required: true }) label = '';
  @Input({ required: true }) valor = 0;
  @Input({ required: true }) icone = '';
  @Input() tom: 'primary' | 'success' | 'neutral' = 'primary';
}
