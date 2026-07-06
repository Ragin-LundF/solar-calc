import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { ApiService } from '@/core/api/api.service';
import { AppStateService } from '@/core/state/app-state.service';
import { ZardButtonComponent } from '@/shared/components/button';
import { ZardCardComponent } from '@/shared/components/card';
import { ZardBadgeComponent } from '@/shared/components/badge';

type AllocationCategory = 'HOUSEHOLD' | 'HEAT_PUMP' | 'WALLBOX';

interface AllocationPolicyDto {
  id?: number;
  priorityOrder: AllocationCategory[];
}

@Component({
  selector: 'app-allocation-policy',
  imports: [RouterLink, TranslatePipe, ZardButtonComponent, ZardCardComponent, ZardBadgeComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './allocation-policy.component.html',
})
export class AllocationPolicyComponent implements OnInit {
  private readonly api = inject(ApiService);
  private readonly state = inject(AppStateService);

  readonly tenantId = this.state.tenantId;
  readonly profileId = this.state.profileId;
  readonly saving = signal(false);
  readonly saved = signal(false);
  readonly error = signal<string | null>(null);
  readonly policyId = signal<number | null>(null);

  readonly allCategories: AllocationCategory[] = ['HOUSEHOLD', 'HEAT_PUMP', 'WALLBOX'];
  readonly priorityOrder = signal<AllocationCategory[]>([...this.allCategories]);

  ngOnInit(): void {
    const tid = this.tenantId();
    const pid = this.profileId();
    if (!tid || !pid) return;

    this.api.get<AllocationPolicyDto[]>(`/tenants/${tid}/profiles/${pid}/allocation-policies`).subscribe({
      next: policies => {
        const p = policies[0];
        if (p) {
          this.policyId.set(p.id ?? null);
          this.priorityOrder.set(p.priorityOrder);
        }
      },
    });
  }

  moveUp(index: number): void {
    if (index === 0) return;
    this.priorityOrder.update(list => {
      const arr = [...list];
      [arr[index - 1], arr[index]] = [arr[index], arr[index - 1]];
      return arr;
    });
    this.saved.set(false);
  }

  moveDown(index: number): void {
    const list = this.priorityOrder();
    if (index === list.length - 1) return;
    this.priorityOrder.update(arr => {
      const copy = [...arr];
      [copy[index + 1], copy[index]] = [copy[index], copy[index + 1]];
      return copy;
    });
    this.saved.set(false);
  }

  save(): void {
    const tid = this.tenantId();
    const pid = this.profileId();
    if (!tid || !pid) return;

    this.saving.set(true);
    this.error.set(null);
    const body: AllocationPolicyDto = { priorityOrder: this.priorityOrder() };
    const id = this.policyId();
    const call = id
      ? this.api.put<AllocationPolicyDto>(`/tenants/${tid}/profiles/${pid}/allocation-policies/${id}`, body)
      : this.api.post<AllocationPolicyDto>(`/tenants/${tid}/profiles/${pid}/allocation-policies`, body);

    call.subscribe({
      next: p => {
        this.policyId.set(p.id ?? null);
        this.saving.set(false);
        this.saved.set(true);
      },
      error: () => { this.saving.set(false); this.error.set('common.error'); },
    });
  }
}
