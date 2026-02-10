import {Component} from '@angular/core';
import {DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE, MAT_NATIVE_DATE_FORMATS, NativeDateAdapter} from '@angular/material/core';
import {ServiceNewComponent} from "../service-new/service-new.component";

@Component({
  selector: 'app-service-edit',
  templateUrl: './service-edit.component.html',
  styleUrls: ['./service-edit.component.css'],
  providers: [
    { provide: MAT_DATE_LOCALE, useValue: 'de-DE' },
    {
      provide: DateAdapter,
      useClass: NativeDateAdapter,
      deps: [MAT_DATE_LOCALE],
    },
    { provide: MAT_DATE_FORMATS, useValue: MAT_NATIVE_DATE_FORMATS },
  ],
  standalone: false
})
export class ServiceEditComponent extends ServiceNewComponent {
  override pageTitle = 'Eintrag bearbeiten';

  get isLoading(): boolean {
    return this.editMode && !this.clientSelected;
  }

  get clientDisplayName(): string {
    if (!this.selectedClient || !this.selectedClient.id) {
      return '';
    }
    return `${this.selectedClient.lastName} ${this.selectedClient.firstName}`;
  }

  override initFormSubscriptions() {
    const preservedGoalIds = this.selectedGoals.map(goal => goal.id);
    const valueGoalIds = this.value?.goals?.map(goal => goal.id) ?? [];
    const effectiveGoalIds = preservedGoalIds.length > 0 ? preservedGoalIds : valueGoalIds;

    if (this.editMode) {
      this.normalizeEditControlValues();
    }

    super.initFormSubscriptions();

    if (this.editMode) {
      this.serviceDateControl.disable({ emitEvent: false });

      const planId = this.ensureAssistancePlanSelection();
      this.emitAssistancePlanSelection(planId, effectiveGoalIds);
      this.restoreGoals(effectiveGoalIds, planId);
      this.ensureClientSelection();
    }
  }

  private normalizeEditControlValues() {
    const clientValue = this.clientsControl.value;
    if (Array.isArray(clientValue)) {
      const nextClientId = this.coerceId(clientValue[0]);
      this.clientsControl.setValue(nextClientId, { emitEvent: false });
    }

    const planValue = this.assistancePlansControl.value;
    if (Array.isArray(planValue)) {
      const nextPlanId = this.coerceId(planValue[0]);
      this.assistancePlansControl.setValue(nextPlanId, { emitEvent: false });
    }
  }

  private ensureAssistancePlanSelection(): number | null {
    const planValue = this.assistancePlansControl.value;
    const fallbackPlanId = this.value?.assistancePlanId ?? this.selectedAssistancePlan?.id ?? null;
    const planId = this.coerceId(planValue ?? fallbackPlanId);

    if ((!planValue || Number(planValue) === 0) && fallbackPlanId != null) {
      this.assistancePlansControl.setValue(Number(fallbackPlanId), { emitEvent: false });
    }

    if (planId != null) {
      const plan = this.filteredAssistancePlans.find(value => value.id === planId);
      if (plan) {
        this.selectAssistancePlan(plan);
        this.assistancePlansControl.setValue(planId, { emitEvent: false });
        return planId;
      }
      this.assistancePlansControl.setValue(planId, { emitEvent: true });
      return planId;
    }

    if ((this.filteredAssistancePlans?.length ?? 0) > 0) {
      const byDate = this.getAssistancePlansByDateString(
        this.filteredAssistancePlans,
        this.selectedServiceDate
      );
      const fallbackPlan = byDate[0] ?? this.filteredAssistancePlans[0];
      if (fallbackPlan) {
        this.selectAssistancePlan(fallbackPlan);
        this.assistancePlansControl.setValue(fallbackPlan.id, { emitEvent: false });
        return fallbackPlan.id;
      }
    }

    return null;
  }

  private emitAssistancePlanSelection(planId: number | null, goalIds: number[]) {
    if (planId == null) {
      return;
    }
    this.assistancePlansControl.setValue(planId, { emitEvent: true });
    if (goalIds.length > 0) {
      Promise.resolve().then(() => {
        this.goalsControl.setValue(goalIds, { emitEvent: false });
        this.setGoals(goalIds);
      });
    }
  }

  private restoreGoals(goalIds: number[], planId: number | null) {
    if (goalIds.length > 0) {
      this.goalsControl.setValue(goalIds, { emitEvent: false });
      this.setGoals(goalIds);
      return;
    }

    if (planId == null) {
      return;
    }

    this.serviceService.getByAssistancePlan(planId).subscribe({
      next: (services) => {
        const match = services.find(service => service.id === this.value?.id);
        const matchGoalIds = (match?.goals ?? []).map(goal => goal.id);
        if (matchGoalIds.length === 0) {
          return;
        }
        this.goalsControl.setValue(matchGoalIds, { emitEvent: false });
        this.setGoals(matchGoalIds);
        this.value.goals = match?.goals ?? [];
        if (this.value?.assistancePlanId === 0 && match?.assistancePlanId) {
          this.value.assistancePlanId = match.assistancePlanId;
        }
      }
    });
  }

  private ensureClientSelection() {
    const clientValue = this.clientsControl.value;
    const fallbackClientId = this.selectedClient?.id ?? null;
    if ((clientValue == null || Number(clientValue) === 0) && fallbackClientId != null) {
      this.clientsControl.setValue(Number(fallbackClientId), { emitEvent: false });
    }
  }

  private coerceId(value: unknown): number | null {
    if (value == null) {
      return null;
    }
    const numeric = Number(value);
    if (!Number.isFinite(numeric) || numeric <= 0) {
      return null;
    }
    return numeric;
  }
}
