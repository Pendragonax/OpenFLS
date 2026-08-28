import '@testbed';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {of, ReplaySubject} from 'rxjs';
import {vi} from 'vitest';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {HourCorridorsComponent} from './hour-corridors.component';
import {HourCorridorDto} from '../../shared/dtos/hour-corridor-dto.model';
import {HourTypeDto} from '../../shared/dtos/hour-type-dto.model';
import {HelperService} from '../../shared/services/helper.service';
import {HourCorridorService} from '../../shared/services/hour-corridor.service';
import {HourTypeService} from '../../shared/services/hour-type.service';
import {Comparer} from '../../shared/services/comparer.helper';

function corridor(overrides: Partial<HourCorridorDto>): HourCorridorDto {
  return new HourCorridorDto({
    id: 1,
    title: 'Default',
    weeklyMinutesFrom: 300,
    weeklyMinutesTill: 600,
    hourTypeId: 7,
    hourTypeTitle: 'Pflege',
    assistancePlanCount: 0,
    ...overrides
  });
}

describe('HourCorridorsComponent', () => {
  let component: HourCorridorsComponent;
  let fixture: ComponentFixture<HourCorridorsComponent>;
  let hourCorridorService: {allValues$: ReplaySubject<HourCorridorDto[]>; create: ReturnType<typeof vi.fn>; update: ReturnType<typeof vi.fn>; delete: ReturnType<typeof vi.fn>};
  let helperService: {openSnackBar: ReturnType<typeof vi.fn>};

  beforeEach(async () => {
    hourCorridorService = {
      allValues$: new ReplaySubject<HourCorridorDto[]>(1),
      create: vi.fn().mockReturnValue(of({})),
      update: vi.fn().mockReturnValue(of({})),
      delete: vi.fn().mockReturnValue(of({}))
    };

    helperService = {
      openSnackBar: vi.fn()
    };

    await TestBed.configureTestingModule({
      declarations: [HourCorridorsComponent],
      schemas: [NO_ERRORS_SCHEMA],
      providers: [
        {provide: NgbModal, useValue: {open: vi.fn(() => ({result: Promise.resolve(true)}))}},
        {provide: HelperService, useValue: helperService},
        {provide: HourCorridorService, useValue: hourCorridorService},
        {provide: HourTypeService, useValue: {allValues$: of([new HourTypeDto({id: 7, title: 'Pflege'})])}},
        {provide: Comparer, useValue: {compare: (a: any, b: any, isAsc: boolean) => (a < b ? -1 : a > b ? 1 : 0) * (isAsc ? 1 : -1)}}
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(HourCorridorsComponent);
    component = fixture.componentInstance;
    hourCorridorService.allValues$.next([corridor({id: 1})]);
    fixture.detectChanges();
  });

  it('maps form values to the corridor payload on create', () => {
    component.titleControl.setValue('Morgens');
    component.hourTypeControl.setValue(7);
    component.weeklyHoursFromPartControl.setValue(3);
    component.weeklyMinutesFromPartControl.setValue(15);
    component.weeklyHoursTillPartControl.setValue(5);
    component.weeklyMinutesTillPartControl.setValue(45);

    component.create(component.editValue);

    expect(hourCorridorService.create).toHaveBeenCalledWith(component.editValue);
    expect(component.editValue.weeklyMinutesFrom).toBe(195);
    expect(component.editValue.weeklyMinutesTill).toBe(345);
  });

  it('blocks deletion when assistance plans are linked', () => {
    const linked = corridor({assistancePlanCount: 2});

    component.delete(linked);

    expect(hourCorridorService.delete).not.toHaveBeenCalled();
    expect(helperService.openSnackBar).not.toHaveBeenCalled();
    expect(component.isDeleteDisabled(linked)).toBe(true);
    expect(component.getDeleteHint(linked)).toBe('2 Hilfepläne verknüpft');
  });

  it('onSearchStringChanges_filtersByTitleAndHourType', () => {
    component.hourTypes = [];
    component.values = [
      corridor({id: 1, title: 'Morgens', hourTypeTitle: 'Pflege'}),
      corridor({id: 2, title: 'Spät', hourTypeTitle: 'Hauswirtschaft'})
    ];

    component.onSearchStringChanges('pflege');

    expect(component.filteredTableData.map(value => value.id)).toEqual([1]);
  });

  it('deletes unlinked corridors through the service', () => {
    const unlinked = corridor({assistancePlanCount: 0});

    component.delete(unlinked);

    expect(hourCorridorService.delete).toHaveBeenCalledWith(unlinked.id);
  });

  it('splits stored minute values into the edit form', () => {
    component.fillEditForm(corridor({weeklyMinutesFrom: 125, weeklyMinutesTill: 245}));

    expect(component.weeklyHoursFromPartControl.value).toBe(2);
    expect(component.weeklyMinutesFromPartControl.value).toBe(5);
    expect(component.weeklyHoursTillPartControl.value).toBe(4);
    expect(component.weeklyMinutesTillPartControl.value).toBe(5);
  });

  it('openEditModal_whenReopened_withDifferentTillValue_restoresSelectedTillValue', () => {
    component.openEditModal({} as any, corridor({weeklyMinutesFrom: 125, weeklyMinutesTill: 245}));
    component.openEditModal({} as any, corridor({weeklyMinutesFrom: 180, weeklyMinutesTill: 345}));

    expect(component.weeklyHoursTillPartControl.value).toBe(5);
    expect(component.weeklyMinutesTillPartControl.value).toBe(45);
    expect(component.editValue.weeklyMinutesTill).toBe(345);
  });

  it('renders corridor cards for the mobile layout', () => {
    fixture.detectChanges();

    const cards = fixture.nativeElement.querySelectorAll('.corridor-card');

    expect(cards.length).toBe(1);
    expect(cards[0].textContent).toContain('Default');
    expect(cards[0].textContent).toContain('Pflege');
  });

  it('sorts by hour type title and linked assistance plan count', () => {
    component.hourTypes = [];
    component.tableSource.data = [
      corridor({id: 1, hourTypeTitle: 'Zwei', assistancePlanCount: 3}),
      corridor({id: 2, hourTypeTitle: 'Eins', assistancePlanCount: 1})
    ];

    component.sortData({active: 'hourType', direction: 'asc'} as any);
    expect(component.tableSource.data[0].hourTypeTitle).toBe('Eins');

    component.sortData({active: 'assistancePlanCount', direction: 'desc'} as any);
    expect(component.tableSource.data[0].assistancePlanCount).toBe(3);
  });
});
