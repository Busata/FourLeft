import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DiscordIntegrationContainerComponent } from './discord-integration-container.component';

describe('DiscordIntegrationContainerComponent', () => {
  let component: DiscordIntegrationContainerComponent;
  let fixture: ComponentFixture<DiscordIntegrationContainerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DiscordIntegrationContainerComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DiscordIntegrationContainerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
