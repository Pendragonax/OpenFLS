import {Component, Input, OnInit} from '@angular/core';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {HourCorridorAssistancePlanDto} from '../../shared/dtos/hour-corridor-assistance-plan.dto';
import {HourCorridorService} from '../../shared/services/hour-corridor.service';
@Component({selector:'app-hour-corridor-assistance-plans',templateUrl:'./hour-corridor-assistance-plans.component.html',styleUrls:['./hour-corridor-assistance-plans.component.css'],standalone:false})
export class HourCorridorAssistancePlansComponent implements OnInit {
  @Input() corridorTitle=''; @Input() corridorId=0; plans: HourCorridorAssistancePlanDto[]=[]; isLoading=true; hasError=false;
  constructor(private service:HourCorridorService, public activeModal:NgbActiveModal){}
  ngOnInit(){this.service.getAssistancePlans(this.corridorId).subscribe({next:p=>{this.plans=p;this.isLoading=false;},error:()=>{this.hasError=true;this.isLoading=false;}});}
}
