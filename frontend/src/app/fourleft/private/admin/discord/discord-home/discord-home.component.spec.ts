import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DiscordHomeComponent } from './discord-home.component';

describe('DiscordHomeComponent', () => {
  let component: DiscordHomeComponent;
  let fixture: ComponentFixture<DiscordHomeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DiscordHomeComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DiscordHomeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
