import {Component, EventEmitter, Input, OnInit, Output, SimpleChanges} from '@angular/core';
import {Observable} from "rxjs";
import {map, startWith} from 'rxjs/operators';
import {FormControl} from "@angular/forms";
import {ClientSoloDto} from "../../dtos/client-solo-dto.model";

@Component({
    selector: 'app-client-autocomplete',
    templateUrl: './client-autocomplete.component.html',
    styleUrl: './client-autocomplete.component.css',
    standalone: false
})
export class ClientAutocompleteComponent implements OnInit {

  @Input() clients: ClientSoloDto[] = [];
  @Input() clientId: number | null = null;
  @Input() disabled: boolean = false;

  @Output() clientChanged: EventEmitter<ClientSoloDto | null> = new EventEmitter<ClientSoloDto | null>();

  client: ClientSoloDto | null = null;
  clientControl: FormControl;
  filteredClients$!: Observable<ClientSoloDto[]>;

  constructor() {
    this.clientControl = new FormControl({value: this.client, disabled: this.disabled})
  }

  ngOnInit() {
    this.filteredClients$ = this.clientControl.valueChanges.pipe(
      startWith(''),
      map(value => this._filter(value ?? "")),
    );

    this.clientControl.valueChanges.subscribe(value => {
      if (isClientSoloDto(value)) {
        this.client = value;
        this.clientChanged.emit(value);
      } else if (value == null) {
        this.clientChanged.emit(value);
      }
    })
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['disabled']) {
      const disabled = changes['disabled'].currentValue;
      if (disabled) {
        this.clientControl.disable({ emitEvent: false });
      } else {
        this.clientControl.enable({ emitEvent: false });
      }
    }

    if (changes['clients']) {
      if (this.clientId !== null && this.clients != null) {
        this.client = this.clients.find(it => it.id == this.clientId) ?? null;
        this.clientControl.setValue(this.client);
      }
    }
  }

  reset(event: MouseEvent) {
    event.stopPropagation();
    this.clientControl.reset();
  }

  displayFn(client: any): string {
    if (isClientSoloDto(client)) {
      return client ? getFullName(client) : '';
    }

    return '';
  }

  private _filter(value: any): ClientSoloDto[] {
    if (typeof value !== 'string') {
      return [];
    }

    const filterValue = value.toLowerCase();
    return this.clients.filter(option =>
      getFullName(option).toLowerCase().includes(filterValue.toLowerCase())
    );
  }

  protected readonly getFullName = getFullName;
}

function isClientSoloDto(client: any): client is ClientSoloDto {
  return client && typeof client === 'object' && 'firstName' in client && 'lastName' in client;
}

function getFullName(client: ClientSoloDto): string {
  return client.lastName + " " + client.firstName;
}
