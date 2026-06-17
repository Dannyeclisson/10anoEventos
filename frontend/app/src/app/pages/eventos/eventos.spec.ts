import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { EventoService } from '../../services/evento.service';
import { EventosComponent } from './eventos';

describe('EventosComponent', () => {
  let component: EventosComponent;
  let fixture: ComponentFixture<EventosComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EventosComponent],
      providers: [
        provideRouter([]),
        {
          provide: EventoService,
          useValue: {
            confirmados$: of([]),
            listar: () => of({ eventos: [] }),
            confirmarParticipacao: () => undefined
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(EventosComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
