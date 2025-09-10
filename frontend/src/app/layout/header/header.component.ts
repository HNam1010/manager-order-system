import { Component, OnInit, OnDestroy } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { RouterLink, RouterModule, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { CommonModule } from '@angular/common';

import { AuthService } from '../../service/auth.service'; // Đúng đường dẫn
import { UserInfo } from '../../models/auth.model'; // Đúng đường dẫn
import { CartService } from '../../service/cart.service'; // Nếu cần hiển thị số lượng cart



@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit, OnDestroy {


  isDropdownOpen = false; // để hiển thị menu khi đăng nhập

  constructor(
    public authService: AuthService,
    public cartService: CartService // <-- INJECT CartService và đặt là public
  ) { }

  ngOnInit(): void {
  
  }

  ngOnDestroy(): void {
  }

  logout(): void {
    this.authService.logout();

  }


  toggleDropdown(): void {
    this.isDropdownOpen = !this.isDropdownOpen;
  }

}