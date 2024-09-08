import { Component, OnInit } from '@angular/core';
import {SponsorDto} from "../../../../shared/dtos/sponsor-dto.model";
import {ActivatedRoute} from "@angular/router";
import {SponsorService} from "../../../../shared/services/sponsor.service";
import {ReplaySubject, Subject} from "rxjs";
import {UnprofessionalDto} from "../../../../shared/dtos/unprofessional-dto.model";

@Component({
  selector: 'app-sponsor-detail',
  templateUrl: './sponsor-detail.component.html',
  styleUrls: ['./sponsor-detail.component.css']
})
export class SponsorDetailComponent implements OnInit {
  sponsor: SponsorDto | null = null;
  sponsor$: ReplaySubject<SponsorDto> = new ReplaySubject<SponsorDto>();
  noProfessionals$: Subject<UnprofessionalDto[]> = new Subject<UnprofessionalDto[]>();

  isSubmitting: boolean = false;

  constructor(
    private sponsorService: SponsorService,
    private route: ActivatedRoute) { }

  ngOnInit() {
    this.loadSponsor();
  }

  loadSponsor() {
    const id = this.route.snapshot.paramMap.get('id');

    if (id == null || this.isSubmitting) return;

    this.isSubmitting = true;

    this.sponsorService.getById(+id).subscribe({
      next: (value) => {
        this.sponsor = value;
        this.sponsor$.next(value);
        this.noProfessionals$.next(value.unprofessionals);
        this.isSubmitting = false;
      },
      error: () => this.isSubmitting = false
    })
  }
}
