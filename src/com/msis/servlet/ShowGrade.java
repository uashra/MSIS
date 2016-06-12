package com.msis.servlet;

import com.msis.DBConnection.*;
import com.msis.model.StudentModel;

import java.sql.*;
import java.util.ArrayList;
import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;



/**
 * Servlet implementation class ShowGrade
 */
@WebServlet("/ShowGrade")
public class ShowGrade extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ShowGrade() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String selected_term = request.getParameter("selected_term");
		int studentId = 0;
		boolean validStudent= false;
		int termId = Integer.parseInt(selected_term);
		HttpSession session = request.getSession(true);
		
		if(session.getAttribute("userType").equals("admin")){
			String selected_student = request.getParameter("studentId");
			studentId = Integer.parseInt(selected_student);
			StudentModel studentModel = new StudentModel();
			validStudent = studentModel.validateStudent(studentId);
			if(validStudent == false){
				// Send the data into JSP page
				RequestDispatcher requestDispatcher = request.getRequestDispatcher("view_grade.jsp");
				request.setAttribute("errorMsg", "Sorry! This student id does not exit!");
				
				requestDispatcher.forward(request, response);
			}
			
		}else {
			studentId = (Integer)session.getAttribute("userId");
		}
		
		
		System.out.println(termId);
	
		MySQLAccess obj = new MySQLAccess();
		Connection connection = obj.getConnection();

		
		// select all the courses and related info for a registered term and student id
//		String sql = "SELECT ti.id, ti.term, CONCAT(sbj.subject_code,'-',cs.course_code,' ', cs.title) as course_title, gpa, grade_scale," +
//		" cs.units as unit, gpa*cs.units as point FROM registration_cart rgs, course_details cd, term_info ti, course cs, subject sbj, grade grd, grading_points gp" + 
//		" where rgs.student_id="+studentId+ 
//		" and cs.id = cd.course_id " +  
//		" and cd.id=rgs.course_details_id " + 
//		" and cs.subject_id=sbj.id " +  
//		" and cd.term_id=ti.id " + 
//		" and cd.term_id="+termId+
//		" and grd.course_id=rgs.course_details_id " +
//		" and grd.gpa=gp.points";
		
		String sql = "select distinct course_details_id, CONCAT( sbj.subject_code, '-', cs.course_code, ' ', cs.title ) AS course_title, ti.term, "+
				" IF(EXISTS(select gpa from grade where course_id=rg.course_details_id), "+ 
				" (select gpa from grade where course_id=rg.course_details_id), '') as gpa, "+ 
				" (select grade_scale from grading_points grp, grade g where g.gpa = grp.points and g.course_id=rg.course_details_id) as grade_scale,"+
				" cs.units as unit " +
				" from registration_cart rg, grade grd, course cs, course_details cd, term_info ti, subject sbj,grading_points gp" +
				" where rg.student_id="+studentId+
				" and rg.course_details_id=cd.id" +
				" and cd.course_id=cs.id" +
				" and cd.term_id=ti.id" +
				" and cs.subject_id= sbj.id" +
				" and cd.term_id="+termId+
				" and grd.gpa = gp.points";
		
		String term_name = null;
		String point;
		PreparedStatement prepareStm;
		try {
			prepareStm = connection.prepareStatement(sql);
			ResultSet results = prepareStm.executeQuery();
			//make an String type arrayList containg all the info about course and GPA
			ArrayList<ArrayList<String>> Rows = new ArrayList<ArrayList<String>>();
			while(results.next()){
				System.out.println(results.getString("course_title"));
				System.out.println(results.getString("gpa"));
				System.out.println(results.getString("grade_scale"));
				term_name = results.getString("term");
				
				ArrayList<String> row = new ArrayList<String>();
				for (int i = 1; i <= 1 ; i++){
			    	row.add(results.getString("course_title"));
			    	row.add(results.getString("gpa"));
			    	row.add(results.getString("grade_scale"));
			    	row.add(results.getString("unit"));
			    	//row.add(results.getString("point"));
			    	
			    	if(!results.getString("gpa").equals("") && results.getString("grade_scale") != null){
			    		point = (String.valueOf(Double.parseDouble(results.getString("gpa")) * Double.parseDouble(results.getString("unit"))));
			    		row.add(point);
			    	} else {
			    		row.add("");
			    	}
			    	
			    }
				
				
			    Rows.add(row);
			}
			
			// Send the data into JSP page
			RequestDispatcher requestDispatcher = request.getRequestDispatcher("your_grade.jsp");
			request.setAttribute("courseList", Rows);
			request.setAttribute("term_name", term_name);
			requestDispatcher.forward(request, response);
		} catch (SQLException e) {
			System.out.println("Something went wrong. Please contact system admin.");
			System.err.println(e.getMessage());
		}
	}

}
