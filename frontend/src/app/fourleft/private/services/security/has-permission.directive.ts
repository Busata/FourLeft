import {Directive, ElementRef, Input, OnInit} from "@angular/core";
import {User, UserStoreService} from "./user-store.service";


@Directive({
  selector: "[hasPermission]"
})
export class HasPermissionDirective implements OnInit {

  @Input("hasPermission") permission: string = "";

  constructor(private elementRef: ElementRef, private userStoreService: UserStoreService) {
  }

  ngOnInit(): void {
    this.elementRef.nativeElement.style.display = "none";

    this.userStoreService.user.subscribe(user => {
      this.checkAccess(user);
    })
  }

  private checkAccess(user: User) {

    if (user.roles.indexOf(this.permission) != -1) {
      this.elementRef.nativeElement.style.display = "";
    } else {
      this.elementRef.nativeElement.style.display = "none";
    }

  }
}
