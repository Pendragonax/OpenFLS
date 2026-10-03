import {Component, EventEmitter, Input, OnInit, Output, SimpleChanges} from '@angular/core';
import {Observable} from "rxjs";
import {map, startWith} from 'rxjs/operators';
import {FormControl} from "@angular/forms";
import {ClientSelectionDto} from "../../dtos/client-selection-dto.model";

@Component({
    selector: 'app-client-autocomplete',
    templateUrl: './client-autocomplete.component.html',
    styleUrl: './client-autocomplete.component.css',
    standalone: false
})
export class ClientAutocompleteComponent implements OnInit {

  @Input() clients: ClientSelectionDto[] = [];
  @Input() clientId: number | null = null;
  @Input() disabled: boolean = false;

  @Output() clientChanged: EventEmitter<ClientSelectionDto | null> = new EventEmitter<ClientSelectionDto | null>();

  client: ClientSelectionDto | null = null;
  clientControl: FormControl;
  filteredClients$!: Observable<ClientSelectionDto[]>;

  constructor() {
    this.clientControl = new FormControl({value: this.client, disabled: this.disabled})
  }

  ngOnInit() {
    this.filteredClients$ = this.clientControl.valueChanges.pipe(
      startWith(''),
      map(value => this._filter(value ?? "")),
    );

    this.clientControl.valueChanges.subscribe(value => {
      if (isClientSelectionDto(value)) {
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
    if (isClientSelectionDto(client)) {
      return client ? getFullName(client) : '';
    }

    return '';
  }

  private _filter(value: any): ClientSelectionDto[] {
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

function isClientSelectionDto(client: any): client is ClientSelectionDto {
  return client && typeof client === 'object' && 'firstName' in client && 'lastName' in client;
}

function getFullName(client: ClientSelectionDto): string {
  return client.lastName + " " + client.firstName;
}
