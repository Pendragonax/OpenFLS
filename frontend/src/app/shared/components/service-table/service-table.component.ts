import {
  AfterViewInit,
  Component,
  EventEmitter,
  Input,
  OnChanges,
  Output,
  SimpleChanges,
  ViewChild
} from '@angular/core';
import {MatTableDataSource} from "@angular/material/table";
import {MatSort} from "@angular/material/sort";
import {Service} from "../../dtos/service.projection";
import {PageEvent} from "@angular/material/paginator";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {UserService} from "../../services/user.service";
import {ReplaySubject} from "rxjs";
import {CsvService} from "../../services/csv.service";
import {ServiceExport} from "./model/service-export.model";
import {ServiceService} from "../../services/service.service";

@Component({
    selector: 'app-service-table',
    templateUrl: './service-table.component.html',
    styleUrl: './service-table.component.css',
    standalone: false
})
export class ServiceTableComponent implements AfterViewInit, OnChanges {
  @Input() services: Service[] = [];
  @Input() editMode: boolean = false;
  @Input() adminMode: boolean = true;
  @Input() redRows: boolean = false;
  @Output() tableUpdated = new EventEmitter();

  @ViewChild(MatSort) sort!: MatSort;

  tableDataSource = new MatTableDataSource<any>([]);
  displayedColumns: string[] = [];
  pageLength: number = 0;
  pageSize: number = 100;
  pageIndex: number = 0;
  isAdmin$: ReplaySubject<boolean> = new ReplaySubject<boolean>(1);

  private static readonly COLUMN_VIEW_MAPPING = {
    start: 'Zeitpunkt',
    content: 'Inhalt',
    institutionName: 'Bereich',
    employeeFullName: 'Mitarbeiter',
    clientFullName: 'Klient'
  };

  private static readonly COLUMN_EDIT_MAPPING = {
    start: 'Zeitpunkt',
    minutes: 'Minuten',
    content: 'Inhalt',
    clientFullName: 'Klient'
  };

  constructor(private modalService: NgbModal,
              private userService: UserService,
              private serviceService: ServiceService,
              private csvService: CsvService) {
    this.userService.isAdmin$.subscribe(value => this.isAdmin$.next(value))
  }

  ngAfterViewInit(): void {
    if (this.sort) {
      this.sort.sortChange.subscribe(() => {
        this.pageIndex = 0; // Reset to first page on sort change
        this.updateTableDataSource();
      });
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['services'] || changes['editMode'] || changes['adminMode']) {
      this.updateTableDataSource();
    }
  }

  private updateTableDataSource(): void {
    this.pageLength = this.services.length;
    this.pageIndex = 0;
    this.applySort();
    this.setPage(this.pageIndex);

    if (this.services.length > 0) {
      if (this.editMode) {
        this.displayedColumns = [...Object.keys(ServiceTableComponent.COLUMN_EDIT_MAPPING), 'actions'];
      } else {
        if (this.adminMode) {
          this.displayedColumns = [...Object.keys(ServiceTableComponent.COLUMN_VIEW_MAPPING), 'actions'];
        } else {
          this.displayedColumns = [...Object.keys(ServiceTableComponent.COLUMN_VIEW_MAPPING)];
        }
      }
    } else {
      this.tableDataSource.data = [];
      this.displayedColumns = [];
    }
  }

  private mapServiceToDataSource(service: Service): any {
    return {
      id: service.id,
      start: service.start,
      startFormatted: this.transformDateString(service.start),
      minutes: service.minutes,
      content: (service.title.length > 0 ? `<strong>${service.title}</strong><br>` : '') + this.transformLineBreaksToHtml(service.content),
      institutionName: service.institution.name,
      employeeId: service.employee.id,
      employeeFullName: `${service.employee.firstname} ${service.employee.lastname}`,
      clientFullName: `${service.client.firstName} ${service.client.lastName}`
    };
  }

  getColumnDisplayName(column: string): string {
    if (this.editMode) {
      return ServiceTableComponent.COLUMN_EDIT_MAPPING[column] || column;
    }

    return ServiceTableComponent.COLUMN_VIEW_MAPPING[column] || column;
  }

  getFormattedCellContent(column: string, value: any): string {
    if (column === 'start') {
      return this.transformDateString(value);
    }
    return value;
  }

  handlePageEvent(e: PageEvent) {
    if (e != null) {
      this.pageIndex = e.pageIndex;
      this.pageSize = e.pageSize;
      this.setPage(e.pageIndex);
    }
  }

  setPage(pageIndex: number) {
    let targetLength = pageIndex * this.pageSize + this.pageSize;

    if (targetLength >= this.services.length) {
      targetLength = this.services.length;
    }

    const servicesInPage = this.services.slice(pageIndex * this.pageSize, targetLength);
    this.tableDataSource.data = servicesInPage.map(it => this.mapServiceToDataSource(it))
  }

  openDeleteConfirmation(content, value: string) {
    this.modalService
      .open(content, { ariaLabelledBy: 'modal-basic-delete-title', scrollable: true })
      .result
      .then((result: Boolean) => {
        if (result)
          this.serviceService.delete(+value).subscribe({
            next: () => this.tableUpdated.emit(),
            error: () => console.log("Fehler beim speichern")
          });
      })
  }

  applySort() {
    if (this.sort && this.sort.active && this.sort.direction) {
      this.services.sort((a, b) => {
        const isAsc = this.sort.direction === 'asc';
        switch (this.sort.active) {
          case 'start':
            return this.compare(a.start, b.start, isAsc);
          case 'minutes':
            return this.compare(a.minutes, b.minutes, isAsc);
          case 'content':
            return this.compare(a.content, b.content, isAsc);
          case 'institutionName':
            return this.compare(a.institution.name, b.institution.name, isAsc);
          case 'employeeFullName':
            return this.compare(`${a.employee.firstname} ${a.employee.lastname}`, `${b.employee.firstname} ${b.employee.lastname}`, isAsc);
          case 'clientFullName':
            return this.compare(`${a.client.firstName} ${a.client.lastName}`, `${b.client.firstName} ${b.client.lastName}`, isAsc);
          default:
            return 0;
        }
      });
    }
  }

  compare(a: string | number, b: string | number, isAsc: boolean) {
    return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
  }

  transformDateString(value: string): string {
    const date = new Date(value);
    const day = date.getDate().toString().padStart(2, '0');
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const year = date.getFullYear();
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    return `${day}.${month}.${year}<br> ${hours}:${minutes}`;
  }

  private transformLineBreaksToHtml(text: string): string {
    return text.replace(/(?:\r\n|\r|\n)/g, '<br>');
  }

  public exportAsCSV() {
    const serviceExports = this.services.map(value => ServiceExport.arrayStringOf(value))
    this.csvService.exportToCsvWithHeader("export", serviceExports, ServiceExport.header())
  }
}
