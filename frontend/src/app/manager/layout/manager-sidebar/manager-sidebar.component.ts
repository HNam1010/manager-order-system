import { Component } from '@angular/core';
import { Router } from '@angular/router'; // Import Router
import { RouterModule } from '@angular/router'; 


@Component({
  selector: 'app-manager-sidebar',
  imports: [RouterModule],
  templateUrl: './manager-sidebar.component.html',
  styleUrl: './manager-sidebar.component.scss'
})
export class ManagerSidebarComponent {
  constructor(private router: Router) {} // Inject Router để điều hướng sau khi logout

  logout(): void {
    console.log('Đăng xuất...');    
    // Sau khi logout, điều hướng về trang chủ
    this.router.navigate(['/home']); 
  }
}
