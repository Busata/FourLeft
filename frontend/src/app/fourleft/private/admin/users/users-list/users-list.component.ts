import {Component, OnInit} from '@angular/core';
import {User} from "../user";
import {UsersService} from "../users.service";

@Component({
  selector: 'app-users-list',
  templateUrl: './users-list.component.html',
  styleUrls: ['./users-list.component.scss']
})
export class UsersListComponent implements OnInit {

  public users: User[] = [];

  public racenet: string = "";
  public alias: string = "";

  constructor(private userService: UsersService) {
  }

  ngOnInit(): void {
    this.userService.getUsers().subscribe(users => {
      this.users = users;
    })
  }

  deleteUser(user: User) {
    if(confirm("Are you sure?")) {
      this.userService.deleteUser(user).subscribe(() => {
        this.users = this.users.filter(u => u.id !== user.id);
      })
    }
  }

  createUser(racenet: string, alias: string) {
    this.userService.createUser(racenet, alias).subscribe(user => {
      this.users = [...this.users, user];

      this.reset();
    })
  }

  public reset() {
    this.racenet = '';
    this.alias = '';
  }

}
