import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { EventoService, Evento } from '../../services/evento.service';

@Component({
  selector: 'app-eventos',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './eventos.html'
})
export class EventosComponent implements OnInit {

  eventos: Evento[] = [];

  constructor(private service: EventoService) {}

  ngOnInit(): void {
    this.service.listar().subscribe(data => {
      this.eventos = data; 
    });
  }
}